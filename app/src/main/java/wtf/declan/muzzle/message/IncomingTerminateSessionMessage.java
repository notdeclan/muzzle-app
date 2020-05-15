package wtf.declan.muzzle.message;

public class IncomingTerminateSessionMessage extends Message {

    public IncomingTerminateSessionMessage(Message message) {
        super(MessageType.TERMINATE_SESSION, message);
    }

}
