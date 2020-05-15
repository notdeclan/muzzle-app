package wtf.declan.muzzle.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import wtf.declan.muzzle.data.db.converters.DateConverter;
import wtf.declan.muzzle.data.db.converters.MessageTypeConverter;
import wtf.declan.muzzle.message.MessageType;


@Entity(tableName = "messages")
public class MessageEntity implements Comparable<MessageEntity> {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long conversationId;

    @NonNull
    private String body;

    @NonNull
    @TypeConverters(DateConverter.class)
    private Date date;

    // Whether client received (true) or sent the message (false)
    private boolean inbox;

    // if the client has read the message (ie Mark As Read or viewed ConversationActivity)
    private boolean read;

    // If message has had a notification
    private boolean notified;

    // If message has received delivery callback
    private boolean delivered;

    // If message has successfully sent
    private boolean sent;

    // If message has failed
    private boolean failed;

    @NonNull
    @TypeConverters(MessageTypeConverter.class)
    private MessageType messageType;

    public MessageEntity(@NonNull String body, @NonNull Date date, @NonNull MessageType messageType, boolean inbox) {
        this.body               = body;
        this.date               = date;
        this.inbox              = inbox;
        this.messageType        = messageType;

        this.delivered          = false;
        this.sent               = false;
        this.failed             = false;
    }

    @NonNull
    public String getBody() {
        return body;
    }

    public void setBody(@NonNull String body) {
        this.body = body;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public boolean isInbox() {
        return inbox;
    }

    public boolean isOutbox() {
        return !inbox;
    }

    @Override
    public int compareTo(MessageEntity other) {
        // DESCENDING ORDER
        return (int) (other.date.getTime() - this.date.getTime());
    }

    public void setConversationId(long id) {
        this.conversationId = id;
    }

    @NonNull
    public MessageType getMessageType() {
        return messageType;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }


    public long getId() {
        return id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public boolean isNotified() {
        return notified;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isRead() {
        return read;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
