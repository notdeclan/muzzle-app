package wtf.declan.muzzle.taskqueue.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import wtf.declan.muzzle.cryptography.MessageEncryption;
import wtf.declan.muzzle.cryptography.SessionCipher;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.entities.SessionEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.exceptions.EncryptionException;
import wtf.declan.muzzle.exceptions.SessionException;
import wtf.declan.muzzle.message.Message;
import wtf.declan.muzzle.message.OutgoingEncryptedMessage;
import wtf.declan.muzzle.message.OutgoingTerminateSessionMessage;
import wtf.declan.muzzle.data.receivers.MessageDeliveredReceiver;
import wtf.declan.muzzle.data.receivers.MessageSentReceiver;
import wtf.declan.muzzle.taskqueue.ContextTask;

/**
 * Task is used to store messages in the database and send them to the recipient
 * Class also handles message encryption
 */
public class SendMessageTask extends ContextTask {

    private static final String TAG = SendMessageTask.class.getSimpleName();

    private final Message message;
    private final ConversationRepository  conversationRepository;

    public SendMessageTask(Context context, Message message) {
        super(context);

        this.message                    = message;
        this.conversationRepository     = new ConversationRepository(getContext());
    }

    @Override
    public void onRun() throws EncryptionException, SessionException, CloneNotSupportedException {
        Log.w(TAG, "onSend: " + message.getClass().getSimpleName() + " to" + message.getRecipient().getDisplayName());

        // Store Message
        Pair<Long, Long> conversationAndMessageId = storeMessage();

        // Send Message
        if(message instanceof OutgoingEncryptedMessage || message instanceof OutgoingTerminateSessionMessage) {
            Message messageToEncrypt = (Message) message.clone();
            sendEncryptedMessage(messageToEncrypt, conversationAndMessageId.second);
        } else {
            send(message, conversationAndMessageId.second);
        }
    }

    /**
     * Store Message inside database and return the id's for the conversation and message
     * @return: conversation id long /message id long
     */
    private Pair<Long, Long> storeMessage() {
        return conversationRepository.storeMessage(message.getRecipient().getNumber(),
                new MessageEntity(
                        message.getBody(),
                        message.getDate(),
                        message.getMessageType(),
                        false
                )
        );
    }

    private void sendEncryptedMessage(Message encryptedMessage, long messageId) throws SessionException, EncryptionException {
        encryptedMessage = encryptMessage(encryptedMessage);
        send(encryptedMessage, messageId);
    }

    private Message encryptMessage(Message outgoingMessage) throws EncryptionException, SessionException {
        Log.d(TAG, "Encrypting Message");

        ConversationEntity      conversationEntity      = conversationRepository.getConversation(
                outgoingMessage.getRecipient().getNumber()
        );

        // Get Session from Database
        SessionEntity           sessionEntity           = conversationEntity.getSession();

        // Create Session object from Entity
        SessionCipher session                           = new SessionCipher(sessionEntity);

        // Creation Encryption Instance from Session
        MessageEncryption       encryption              = new MessageEncryption(session);

        // Encrypt Message
        Message message = encryption.encrypt(outgoingMessage);

        // Destory Session if needed
        if(outgoingMessage instanceof OutgoingTerminateSessionMessage) {
            conversationEntity.setSession(new SessionEntity());
            conversationRepository.update(conversationEntity);
        }

        return message;
    }

    private void send(Message message, long messageId) {
        try {
            ArrayList<String>           messages            = SmsManager.getDefault().divideMessage(message.toString());
            ArrayList<PendingIntent>    sentIntents         = getSentIntents(messages.size(), messageId);
            ArrayList<PendingIntent>    deliveredIntents    = getDeliveredIntents(messages.size(), messageId);

            SmsManager.getDefault().sendMultipartTextMessage(
                    message.getRecipient().getNumber(),
                    null,
                    messages,
                    sentIntents,
                    deliveredIntents
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<PendingIntent> getSentIntents(int count, long messageId) {
        ArrayList<PendingIntent> intents = new ArrayList<>(count);

        for(int i = 0; i < count; i++) {
            intents.add(PendingIntent.getBroadcast(getContext(), 0, sentIntent(messageId), PendingIntent.FLAG_UPDATE_CURRENT));
        }

        return intents;
    }

    private ArrayList<PendingIntent> getDeliveredIntents(int count, long messageId) {
        ArrayList<PendingIntent> intents = new ArrayList<>(count);

        for(int i = 0; i < count; i++) {
            intents.add(PendingIntent.getBroadcast(getContext(), 0, deliveredIntent(messageId), PendingIntent.FLAG_UPDATE_CURRENT));
        }

        return intents;
    }

    private Intent sentIntent(long messageId) {
        Intent intent = new Intent(MessageSentReceiver.MESSAGE_SENT_ACTION, Uri.parse("custom://" + messageId), getContext(), MessageSentReceiver.class);
        intent.putExtra(MessageSentReceiver.MESSAGE_ID_EXTRA, messageId);

        return intent;
    }

    private Intent deliveredIntent(long messageId) {
        Intent intent = new Intent(MessageDeliveredReceiver.MESSAGE_DELIVERED_ACTION, Uri.parse("custom://" + messageId), getContext(), MessageDeliveredReceiver.class);
        intent.putExtra(MessageDeliveredReceiver.MESSAGE_ID_EXTRA, messageId);

        return intent;
    }

    @Override
    public void onException(Exception e) {
        Log.e(TAG, "onCanceled(): ", e);
    }
}
