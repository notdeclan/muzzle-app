package wtf.declan.muzzle.data.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Objects;

import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.taskqueue.tasks.ReceiveMessageTask;

public class SmsReceiver extends BroadcastReceiver {

    private final String TAG = SmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Telephony.Sms.Intents.SMS_RECEIVED_ACTION) && getIncomingMessage(intent) != null) {
            Log.d(TAG, "onReceive: SMS Broadcast Received");
            final Bundle bundle = intent.getExtras();
            if(bundle != null) {
                Muzzle.getInstance(context).getTaskManager().add(
                        new ReceiveMessageTask(context, bundle)
                );
            }

            abortBroadcast();
        }
    }

    private SmsMessage getIncomingMessage(Intent intent) {
        Bundle bundle = intent.getExtras();

        if(bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null)
                return SmsMessage.createFromPdu((byte[]) pdus[0], bundle.getString("format"));
        }

        return null;
    }
}
