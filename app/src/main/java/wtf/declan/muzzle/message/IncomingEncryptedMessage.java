package wtf.declan.muzzle.message;

public class IncomingEncryptedMessage extends Message {

    public IncomingEncryptedMessage(IncomingTextMessage message) {
        super(MessageType.ENCRYPTED, message);
    }

}