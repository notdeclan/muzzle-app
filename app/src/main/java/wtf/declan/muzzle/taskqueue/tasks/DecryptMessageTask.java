package wtf.declan.muzzle.taskqueue.tasks;

import android.content.Context;
import android.util.Log;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.cryptography.MessageEncryption;
import wtf.declan.muzzle.cryptography.SessionCipher;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.entities.SessionEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;
import wtf.declan.muzzle.message.IncomingTerminateSessionMessage;
import wtf.declan.muzzle.message.Message;
import wtf.declan.muzzle.message.OutgoingTerminateSessionMessage;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.taskqueue.ContextTask;

class DecryptMessageTask extends ContextTask {

    private static final String TAG = DecryptMessageTask.class.getSimpleName();

    private Message message;
    private long    messageId;

    DecryptMessageTask(Context context, Message message, long messageId) {
        super(context);
        this.message = message;
        this.messageId = messageId;
    }

    @Override
    public void onRun() throws Exception {
        // LOL lots of variable, maybe i should have used Dependency injection ...

        Recipient               recipient               =   message.getRecipient();

        // Database Repositories
        ConversationRepository  conversationRepository  =   new ConversationRepository(getContext());
        MessageRepository       messageRepository       =   new MessageRepository(getContext());

        // Get message and conversation from database
        ConversationEntity      conversation            =   conversationRepository.getConversation(recipient.getNumber());
        MessageEntity           messageEntity           =   messageRepository.getMessage(messageId);

        // Get Session Cipher from database and decrypt the message
        SessionEntity           sessionEntity           =   conversation.getSession();
        SessionCipher           session                 =   new SessionCipher(sessionEntity);
        MessageEncryption       encryption              =   new MessageEncryption(session);
                                message                 =   encryption.decrypt(message);

        if(message instanceof IncomingTerminateSessionMessage) {
            Log.d(TAG, "Incoming Terminate Session Message from: " + recipient.getDisplayName());
            if (message.getBody().equals(OutgoingTerminateSessionMessage.TERMINATE_SESSION_MESSAGE)) {
                conversation.setSession(new SessionEntity());
                conversationRepository.update(conversation);
            }
        }

        // Update message body to decrypted one
        messageEntity.setBody(message.getBody());
        messageRepository.update(messageEntity);

        Muzzle.getInstance(getContext()).getNotificationFactory().update(conversation.getId());
    }

    @Override
    public void onException(Exception exception) {
        Log.e(TAG, "Message Decryption was cancelled, storing message as received without decryption.", exception);
    }
}