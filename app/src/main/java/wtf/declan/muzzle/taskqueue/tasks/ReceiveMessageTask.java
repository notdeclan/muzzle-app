package wtf.declan.muzzle.taskqueue.tasks;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.cryptography.Base64;
import wtf.declan.muzzle.cryptography.MessageHash;
import wtf.declan.muzzle.cryptography.SessionCipher;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.entities.SessionEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.message.IncomingEncryptedMessage;
import wtf.declan.muzzle.message.IncomingKeyExchangeMessage;
import wtf.declan.muzzle.message.IncomingTerminateSessionMessage;
import wtf.declan.muzzle.message.IncomingTextMessage;
import wtf.declan.muzzle.message.Message;
import wtf.declan.muzzle.message.MessageType;
import wtf.declan.muzzle.message.OutgoingKeyExchangeMessage;
import wtf.declan.muzzle.util.Preferences;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.taskqueue.ContextTask;

import static wtf.declan.muzzle.message.MessageType.values;

/**
 * Task is added to Task Manager whenever a message is received from the SmsReceiver
 *
 * Task Essentially is responsible for determining incoming message types, storing messages and
 * sending them to further tasks to be decrypted.
 */
public class ReceiveMessageTask extends ContextTask {

    private static final String TAG = ReceiveMessageTask.class.getSimpleName();

    private final Object[] pdus;
    private final Bundle bundle;

    public ReceiveMessageTask(Context context, Bundle bundle) {
        super(context);
        this.bundle = bundle;
        this.pdus = (Object[]) bundle.get("pdus");
    }

    @Override
    public void onRun() throws InvalidKeyException {
        Optional<Message> messageObject = assembleMessage(pdus); // attempt to decipher message
        if(messageObject.isPresent()) { // if message returned
            Message                 message                 =   messageObject.get();
            Recipient               recipient               =   message.getRecipient();
            ConversationRepository  conversationRepository  =   new ConversationRepository(getContext());

            Log.i(TAG,"Processing: " + message.getClass().getSimpleName() + "from: " + recipient.getDisplayName());

            // Store message in the database
            Pair<Long, Long> conversationAndMessageId = conversationRepository.storeMessage(recipient.getNumber(), new MessageEntity(
                    message.getBody(),
                    message.getDate(),
                    message.getMessageType(),
                    true
            ));

            if(message instanceof IncomingEncryptedMessage || message instanceof IncomingTerminateSessionMessage) {
                // decrypt message
                Muzzle.getInstance(getContext()).getTaskManager().add(
                        new DecryptMessageTask(getContext(), message, conversationAndMessageId.second)
                );
            } else if(message instanceof IncomingKeyExchangeMessage) {
                // create a session if key exchange
                // TODO: Move this code to it's own Task
                ConversationEntity      conversation            =   conversationRepository.getConversation(conversationAndMessageId.first);
                SessionEntity           sessionEntity           =   conversation.getSession();
                SessionCipher           session                 =   new SessionCipher(sessionEntity);
                byte[]                  decoded                 =   Base64.decode(message.getBody().getBytes());

                if(session.loadRecipientPublicKey(decoded)) { // load the public key from message
                    // if auto accept and we havent generated a public key
                    if(Preferences.sessionAutoAccept(getContext()) && session.getPublicKey() == null) {
                        session.generateKeys(); // generate our own public keys
                        session.doAgreement(); // complete the agreement

                        conversation.setSession(session.getEntity()); // update the session
                        conversationRepository.updateWorkerThread(conversation); // update conversation

                        // send out our newly generated public keys so the sender can accept it
                        Muzzle.getInstance(getContext()).getTaskManager().add(
                                new SendMessageTask(getContext(), new OutgoingKeyExchangeMessage(
                                        recipient, session.getPublicKey()
                                ))
                        );

                    } else if (session.getPublicKey() != null) { // if we havent got a public key
                        if(session.doAgreement()) { // if we can do an agreement (we've already sent a key)
                            Log.d(TAG, "Session agreement complete with: " + recipient.getDisplayName());
                            // TODO: Add notifications for all session update's so it is easier to
                            //      understand by the user.
                        }
                    }

                    // update session, and conversations
                    conversation.setSession(session.getEntity());
                    conversationRepository.updateWorkerThread(conversation);
                }

            } else {
                // send a notification
                Muzzle.getInstance(getContext()).getNotificationFactory().update(conversationAndMessageId.first);
            }
        }
    }

    @Override
    public void onException(Exception e) {
        Log.e(TAG, "onCancelled: ", e);
    }

    /**
     * Function is used to receive an entire message from the broadcast receiver.
     * As multipart messages come in seperate PDU arrays, each of them are iterated and attempted to
     * be parsed into an SmsMessage object from the Telephony API.
     *
     * After a message has been successfully organised and combined, the message is converted into
     * its relevant muzzle message object
     *
     * @param pdus: PDU from broadcast receiver
     * @return Optional if valid message is produced to handle
     */
    private Optional<Message> assembleMessage(Object[] pdus) {
        List<Message> messages = new LinkedList<>();

        for (Object pdu : pdus) {
            SmsMessage  message     =   SmsMessage.createFromPdu((byte[]) pdu, bundle.getString("format"));
            Recipient   recipient   =   RecipientFactory.getRecipientFromNumber(getContext(), message.getOriginatingAddress());

            messages.add(new IncomingTextMessage(recipient, message));
        }

        if (messages.isEmpty()) {
            return Optional.empty();
        }

        IncomingTextMessage message = new IncomingTextMessage(messages);
        Log.d(TAG, message.getBody());

        return Optional.of(determineMessageType(message));
    }

    /**
     * Function is used to decipher and determine what type of message is being received.
     *
     * It relies on hashing the message body and calculating a new message hash, then the message
     * hash is compared to the original message hash that is prepended to the message body
     *
     * @param message: Incoming Message Object
     * @return: Relevant Message Object
     */
    private Message determineMessageType(@NonNull IncomingTextMessage message) {
        // if the message length is smaller than a message hash then it is impossible to be a special
        // message
        if(message.getBody().length() > MessageHash.ENCODED_LENGTH) {
            final String    messageHash     = message.getBody().substring(0, MessageHash.ENCODED_LENGTH);
            final String    body            = message.getBody().substring(MessageHash.ENCODED_LENGTH);

            for(MessageType messageType : values()) {  // Loop through all prefixes
                // hash the body with the prefix
                String calculatedHash = MessageHash.calculateHash(messageType, body);
                if(messageHash.contains(calculatedHash)) {
                    // remove the prefix hash from the body
                    // return the correct message object
                    switch (messageType) {
                        case ENCRYPTED:
                            message.setBody(body);
                            return new IncomingEncryptedMessage(message);
                        case TERMINATE_SESSION:
                            message.setBody(body);
                            return new IncomingTerminateSessionMessage(message);
                        case KEY_EXCHANGE:
                            message.setBody(body);
                            return new IncomingKeyExchangeMessage(message);
                    }
                }
            }
        }

        // either too small to be prefixed, or no prefix hash found in hash location of body
        return message;
    }

}
