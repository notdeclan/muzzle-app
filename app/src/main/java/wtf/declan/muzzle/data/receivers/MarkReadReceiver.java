package wtf.declan.muzzle.data.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.data.db.Database;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;

/**
 * Receiver essentially marks the conversation and the message as read
 *
 * Is fired from the "Mark As Read" action from Message Notifications
 */
public class MarkReadReceiver extends BroadcastReceiver {

    private static String TAG = MarkReadReceiver.class.getSimpleName();

    public static String MARK_READ_NOTIFICATION_ACTION      = "MARK_READ_NOTIFICATION_ACTION";
    public static String CONVERSATION_ID_EXTRA              = "CONVERSATION_ID_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!MARK_READ_NOTIFICATION_ACTION.equals(intent.getAction())) {
            Log.e(TAG, "onReceive: Received unknown action, ignoring.");
            return;
        }

        long conversationId = intent.getLongExtra(CONVERSATION_ID_EXTRA, -999L);

        if(conversationId != -999L) {
            ConversationRepository  conversationRepository  = new ConversationRepository(context);
            MessageRepository       messageRepository       = new MessageRepository(context);

            Database.databaseWriteExecutor.execute(
                    () -> {
                        ConversationEntity conversation = conversationRepository.getConversation(conversationId);
                        if(conversation != null) {
                            List<MessageEntity> messages = messageRepository.getUnreadUnNotifiedMessages(conversationId);

                            // Set Messages to read
                            messages.forEach(messageEntity -> {
                                messageEntity.setRead(true);
                                messageRepository.update(messageEntity);
                            });

                            // Set conversation as read
                            conversation.setRead(true);
                            conversationRepository.update(conversation);

                            // Update the conversation notification
                            Muzzle.getInstance(context).getNotificationFactory().update(conversationId);
                        }
                    }
            );
        }
    }

    public static Intent buildIntent(Context context, long conversationId) {
        return new Intent(context, MarkReadReceiver.class)
                .setAction(MARK_READ_NOTIFICATION_ACTION)
                .putExtra(CONVERSATION_ID_EXTRA, conversationId);
    }
}
