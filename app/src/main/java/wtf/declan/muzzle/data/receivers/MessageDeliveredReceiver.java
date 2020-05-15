package wtf.declan.muzzle.data.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.db.repositories.MessageRepository;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;

import static android.provider.Telephony.Sms.Intents.SMS_DELIVER_ACTION;

/**
 * Receiver is used to handle intents which are fired from the SmsManager when sending Text Messages
 *
 * Essentially if a message delivered intent is received, the message will be marked as delivered
 * within the database.
 */
public class MessageDeliveredReceiver extends BroadcastReceiver {

    private final static String TAG = MessageDeliveredReceiver.class.getSimpleName();

    public static final String MESSAGE_DELIVERED_ACTION = "MESSAGE_DELIVERED_ACTION";
    public static final String MESSAGE_ID_EXTRA = "MESSAGE_ID_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        byte[]  pdu         = intent.getByteArrayExtra("pdu");
        long    messageId   = intent.getLongExtra(MESSAGE_ID_EXTRA, -999);

        if (pdu == null) {
            Log.w(TAG, "No PDU in delivery receipt!");
            return;
        }

        if(messageId == -999) {
            Log.e(TAG, "No MESSAGE_ID_EXTRA in broadcast");
            return;
        }

        MessageRepository       messageRepository           = new MessageRepository(context);
        ConversationRepository  conversationRepository      = new ConversationRepository(context);

        MessageEntity           message                     = messageRepository.getMessage(messageId);
        ConversationEntity      conversation                = conversationRepository.getConversation(message.getConversationId());
        Recipient               recipient                   = RecipientFactory.getRecipientFromNumber(context, conversation.getNumber());

        SmsMessage              smsMessage                  = SmsMessage.createFromPdu(pdu, SMS_DELIVER_ACTION);

        if (smsMessage == null) {
            Log.w(TAG, "Delivery receipt failed to parse!");
            return;
        }

        showReceivedToasts(context, recipient);
        message.setDelivered(true);
        messageRepository.update(message);
    }

    private void showReceivedToasts(Context context, Recipient recipient) {
        context.getMainExecutor().execute(() -> {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.sms_delivery_toast, recipient.getDisplayName()), Toast.LENGTH_SHORT).show();
        });
    }

}
