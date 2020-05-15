package wtf.declan.muzzle.message;

import android.icu.util.Calendar;

import wtf.declan.muzzle.recipient.Recipient;

public class OutgoingTextMessage extends Message {

    public OutgoingTextMessage(Recipient recipient, String message) {
        super(MessageType.TEXT, recipient, message, Calendar.getInstance().getTime());
    }

}
