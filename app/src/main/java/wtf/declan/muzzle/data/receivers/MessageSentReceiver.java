package wtf.declan.muzzle.data.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.data.db.Database;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;
import wtf.declan.muzzle.notifications.NotificationFactory;

/**
 * Receiver is used to handle sent intents from the Telphony API SmsManager
 */
public class MessageSentReceiver extends BroadcastReceiver {

    private static final String TAG = MessageSentReceiver.class.getSimpleName();

    public static final String MESSAGE_SENT_ACTION      = "MESSAGE_DELIVERED_ACTION";
    public static final String MESSAGE_ID_EXTRA         = "MESSAGE_ID_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        long    messageId   = intent.getLongExtra(MESSAGE_ID_EXTRA, -999);

        if(messageId == -999) {
            Log.e(TAG, "No MESSAGE_ID_EXTRA in broadcast");
            return;
        }
        Database.databaseWriteExecutor.execute(() -> {
            NotificationFactory     notificationFactory         = Muzzle.getInstance(context).getNotificationFactory();
            MessageRepository       messageRepository           = new MessageRepository(context);
            ConversationRepository  conversationRepository      = new ConversationRepository(context);

            MessageEntity           message                     = messageRepository.getMessage(messageId);
            ConversationEntity      conversation                = conversationRepository.getConversation(message.getConversationId());

            switch(getResultCode()) {
                case Activity.RESULT_OK:
                    showToast(context, "Message Sent");
                    message.setSent(true);
                    messageRepository.update(message);
                    return;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    showToast(context, "Generic Failure, try disabling Aeroplane Mode");
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    showToast(context, "Message Failed because service is currently unavailable.");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    showToast(context, "Message Failed due to a problem with the SMS Stack.");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    showToast(context, "Radio is currently disabled, try disabling Aeroplane Mode.");
                    break;
            }
            Log.e(TAG, "Message failed to send");

            notificationFactory.notifyFailed(conversation);

            message.setFailed(true);
            messageRepository.update(message);
        });
    }

    private void showToast(@NonNull Context context, String text) {
        // TODO: test if this is still required as this is no longer inside of a Task and may not
        //  and need to be in the main executor
        context.getMainExecutor().execute(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }
}
