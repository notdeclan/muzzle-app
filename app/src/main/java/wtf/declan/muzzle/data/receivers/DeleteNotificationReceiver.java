package wtf.declan.muzzle.data.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.data.db.Database;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;

/**
 * Receiver is called when a notification is swiped away.
 *
 * Essentially it marks the message entity in the database as "notified" which will prevent it
 * from being displayed in future notifications despite it not being read.
 */
public class DeleteNotificationReceiver extends BroadcastReceiver {

    private final static String TAG = DeleteNotificationReceiver.class.getSimpleName();

    public static String DELETE_NOTIFICATION_ACTION     = "DELETE_NOTIFICATION_ACTION";
    public static String CONVERSATION_ID_EXTRA          = "CONVERSATION_ID_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!DELETE_NOTIFICATION_ACTION.equals(intent.getAction())) { // if wrong action received
            Log.e(TAG, "onReceive: Received unknown action, ignoring.");  // ignore dat
            return;
        }

        long conversationId = intent.getLongExtra(CONVERSATION_ID_EXTRA, -999L);

        if(conversationId != -999L) {
            Database.databaseWriteExecutor.execute(
                () -> {
                    MessageRepository messageRepository = new MessageRepository(context);
                    List<MessageEntity> messages = messageRepository.getUnreadUnNotifiedMessages(conversationId);

                    // Set Messages to notified
                    messages.forEach(messageEntity -> {
                        messageEntity.setNotified(true);
                        messageRepository.update(messageEntity);
                    });

                    // Update the conversation notification
                    Muzzle.getInstance(context).getNotificationFactory().update(conversationId);
                }
            );
        }
    }

    public static Intent buildIntent(Context context, long conversationId) {
        return new Intent(context, DeleteNotificationReceiver.class)
                .setAction(DELETE_NOTIFICATION_ACTION)
                .putExtra(CONVERSATION_ID_EXTRA, conversationId);
    }

}