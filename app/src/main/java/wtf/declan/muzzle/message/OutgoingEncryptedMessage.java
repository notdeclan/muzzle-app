package wtf.declan.muzzle.message;

import android.icu.util.Calendar;

import wtf.declan.muzzle.recipient.Recipient;


public class OutgoingEncryptedMessage extends Message {

    public OutgoingEncryptedMessage(Recipient recipient, String message) {
        super(MessageType.ENCRYPTED, recipient, message, Calendar.getInstance().getTime());
    }

}