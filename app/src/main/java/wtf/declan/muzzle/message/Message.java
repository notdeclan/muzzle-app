package wtf.declan.muzzle.message;


import androidx.annotation.NonNull;

import java.util.Date;

import wtf.declan.muzzle.cryptography.MessageHash;
import wtf.declan.muzzle.recipient.Recipient;

public abstract class Message implements Cloneable {

    private final MessageType messageType;
    protected Recipient recipient;
    protected String body;
    protected Date date;

    protected Message(MessageType messageType) {
        this.messageType = messageType;
    }

    protected Message(MessageType messageType, Recipient recipient) {
        this.messageType    = messageType;
        this.recipient      = recipient;
    }

    protected Message(MessageType messageType, Recipient recipient, String body) {
        this.messageType    = messageType;
        this.recipient      = recipient;
        this.body           = body;
    }

    protected Message(MessageType messageType, Message message) {
        this.messageType    = messageType;
        this.recipient      = message.getRecipient();
        this.body           = message.getBody();
        this.date           = message.getDate();
    }

    protected Message(MessageType messageType, Recipient recipient, String body, Date date) {
        this.messageType    = messageType;
        this.recipient      = recipient;
        this.body           = body;
        this.date           = date;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDate() {
        return date;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    private String getPrefixHash() {
        return messageType == null ? "" : MessageHash.calculateHash(messageType, getBody());
    }

    @NonNull
    @Override
    public String toString() {
        return getPrefixHash() + getBody();
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}