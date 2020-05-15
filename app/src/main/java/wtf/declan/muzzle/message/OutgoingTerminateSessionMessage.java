package wtf.declan.muzzle.message;

import android.icu.util.Calendar;

import wtf.declan.muzzle.recipient.Recipient;

public class OutgoingTerminateSessionMessage extends Message {

    // Message is sent encrypted alongside the message hash to make reversing engineering
    // the message type harder. When a Terminate is received the message is decrypted
    // and if this is in the body then the session will be terminated by the client
    public static String TERMINATE_SESSION_MESSAGE = "Terminate Encrypted Session";

    public OutgoingTerminateSessionMessage(Recipient recipient) {
        super(MessageType.TERMINATE_SESSION, recipient, TERMINATE_SESSION_MESSAGE, Calendar.getInstance().getTime());
    }

}
