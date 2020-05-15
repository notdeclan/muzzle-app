package wtf.declan.muzzle.message;

import android.telephony.SmsMessage;

import java.util.Calendar;
import java.util.List;

import wtf.declan.muzzle.recipient.Recipient;

public class IncomingTextMessage extends Message {

    public IncomingTextMessage(List<Message> messages) {
        super(MessageType.TEXT, messages.get(0));

        StringBuilder stringBuilder = new StringBuilder();
        messages.forEach(message -> stringBuilder.append(message.getBody()));

        setBody(stringBuilder.toString());
    }

    public IncomingTextMessage(Recipient recipient, SmsMessage message) {
        super(null, recipient, message.getDisplayMessageBody(), Calendar.getInstance().getTime());
    }

}