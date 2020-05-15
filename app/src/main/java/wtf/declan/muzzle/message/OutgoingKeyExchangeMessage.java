package wtf.declan.muzzle.message;

import java.security.interfaces.ECPublicKey;
import java.util.Date;

import wtf.declan.muzzle.cryptography.Base64;
import wtf.declan.muzzle.recipient.Recipient;

public class OutgoingKeyExchangeMessage extends Message {

    private final ECPublicKey publicKey;

    public OutgoingKeyExchangeMessage(Recipient recipient, ECPublicKey publicKey) {
        super(MessageType.KEY_EXCHANGE);
        this.recipient = recipient;
        this.publicKey = publicKey;
        this.date = new Date();
    }

    @Override
    public String getBody() {
        return new String(Base64.encode(publicKey.getEncoded()));
    }
}
