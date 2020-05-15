package wtf.declan.muzzle.data.services;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.message.OutgoingTextMessage;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.taskqueue.tasks.SendMessageTask;

/**
 * Service is used to handle messages sent from the SEND_RESPOND_VIA_MESSAGE intent
 *
 * i.e. respond-via-message action during incoming calls
 *
 * This service is required by android to meet the requirements of being a default SMS app
 */
public class RespondViaMessageService extends JobIntentService {

    private static final String TAG = RespondViaMessageService.class.getSimpleName();

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (!TelephonyManager.ACTION_RESPOND_VIA_MESSAGE.equals(intent.getAction())) {
            Log.d(TAG, "Received unknown intent: " + intent.getAction());
            return;
        }

        Context     context     = getApplicationContext();                  // context
        String      number      = intent.getDataString();                   // recipient number
        String      content     = intent.getStringExtra(Intent.EXTRA_TEXT); // contents of text

        if(content != null) {
            Recipient recipient = RecipientFactory.getRecipientFromNumber(context, number);
            Muzzle.getInstance(context).getTaskManager().add(
                    new SendMessageTask(context, new OutgoingTextMessage(recipient, content))
            );
        }
    }

}
