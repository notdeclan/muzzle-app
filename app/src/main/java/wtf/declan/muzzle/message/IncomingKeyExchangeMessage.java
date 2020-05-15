package wtf.declan.muzzle.message;

public class IncomingKeyExchangeMessage extends Message {

    public IncomingKeyExchangeMessage(Message message) {
        super(MessageType.KEY_EXCHANGE, message);
    }
}
