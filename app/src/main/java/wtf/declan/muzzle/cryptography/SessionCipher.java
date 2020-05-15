package wtf.declan.muzzle.cryptography;

import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import wtf.declan.muzzle.data.db.entities.SessionEntity;

/**
 * SessionCipher is used for generating session keys, and handling agreements.
 *
 * Elliptic Curve cryptography is in use, utilizing the NIST P-256 curve is used to derive a 256 Bit
 * secret key which the clients can use with AES256 to encrypt/decrypt messages
 *
 * Elliptic Curve was chosen due to the complexity being higher for a far smaller key size compared
 * to RSA. RSA although being very secure, and not requiring a agreement, would require far more
 * messages, and would limit the size of the data to block sizes.
 *
 * Elliptic Curve was also opted for because it allows encrypted transmission without ever exchanging
 * the public key used for encryption and decryption. This has added cryptographic security benefits.
 *
 * Diffie–Hellman is used to perform the agreement
 *
 ******* The agreement process ********
 *
 * 1. When a client requests to make a secure session with a recipient, a new instance of SessionCipher
 * is created with a new SessionEntity.
 *
 * 2. The client will then generate a EC public and private key using the SECP256R1 (NIST P-256)
 * Curve and then send the public key to the recipient.
 *
 * 3. When the recipient has received and agreed to accept the session from the client, they will
 * send back there own EC public key.
 *
 * 4. The client will then verify the public key received using the validation routines included in
 *  SP 800-56A (revision 2) from the following sections
 *      - ECC Full Public-Key Validation Routine in section 5.6.2.3.2
 *      - ECC Partial Public-Key Validation Routine in section 5.6.2.3.3.
 *
 * 5. If the received key is valid, and secure enough to be used, the client will use
 * there own Private Key, and the Recipient Public key to generate a derived secret key.
 *
 * 6. Using their own derived secret key, the client and recipient can encrypt and decrypt
 * messages sent between
 */
public class SessionCipher {

    private static final String TAG = SessionCipher.class.getSimpleName();


    public static final String      PUBLIC_KEY_ALGORITHM                = "EC";

    private static final String     PUBLIC_KEY_AGREEMENT_ALGORITHM      = "ECDH";

    // The secret key algorithm which is used in the Derived Key spec
    public static final String      SECRET_KEY_ALGORITHM                = "AES-256";

    // The hash algorithm which is used to hash the derived key from the EC agreement
    private static final String     SECRET_KEY_HASH_ALGORITHM           = "SHA-256";

    // NIST P-256 curve for agreeing on derived key (Hopefully not back-doored LOL Thanks NSA)
    private static final String     EC_CURVE_ALGORITHM                  = "secp256r1";

    private final SessionEntity     sessionEntity;

    private SecretKey               derivedKey;
    private ECPublicKey             recipientPublicKey;
    private ECPublicKey             publicKey;
    private ECPrivateKey            privateKey;

    /**
     * Initialize the class with a SessionEntity from the Room database
     *
     * @param sessionEntity: entity from the database
     */
    public SessionCipher(SessionEntity sessionEntity) {
        this.sessionEntity          = sessionEntity;

        this.derivedKey             = sessionEntity.getDerivedKey();
        this.recipientPublicKey     = sessionEntity.getRecipientPublicKey();
        this.publicKey              = sessionEntity.getPublicKey();
        this.privateKey             = sessionEntity.getPrivateKey();
    }

    /**
     * Returns the updated room entity so that the database can be updated with agreed keys etc..
     * @return: Updated entity to insert into the database
     */
    public SessionEntity getEntity() {
        sessionEntity.setDerivedKey(derivedKey);
        sessionEntity.setRecipientPublicKey(recipientPublicKey);
        sessionEntity.setPublicKey(publicKey);
        sessionEntity.setPrivateKey(privateKey);
        sessionEntity.setSessionDate(new Date());

        return sessionEntity;
    }

    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    public SecretKey getDerivedKey() {
        return derivedKey;
    }

    public boolean loadRecipientPublicKey(byte[] key) throws InvalidKeyException {
        try {
            KeyFactory          keyFactory  = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM);
            X509EncodedKeySpec  keySpec     = new X509EncodedKeySpec(key);
            ECPublicKey         received    = (ECPublicKey) keyFactory.generatePublic(keySpec);

            if(!verifyPublicKey(received)) {
                throw new InvalidKeyException("Received bad key");
            }
            recipientPublicKey = received;
            return true;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.wtf(TAG, e);
            return false;
        }
    }

    public boolean doAgreement() {
        Log.i(TAG, "doAgreement()");
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance(PUBLIC_KEY_AGREEMENT_ALGORITHM);
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(recipientPublicKey, true);

            byte[] sharedSecret = keyAgreement.generateSecret();

            derivedKey = deriveKey(sharedSecret, recipientPublicKey);
            return true;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyPublicKey(ECPublicKey publicKey) {
        // Stolen from:
        // https://neilmadden.blog/2017/05/17/so-how-do-you-validate-nist-ecdh-public-keys/
        // Thank you xx

        final EllipticCurve curve = publicKey.getParams().getCurve();

        // Verify that the public key is not at point of infinity
        if (ECPoint.POINT_INFINITY.equals(publicKey.getW())) {
            return false;
        }

        final BigInteger affineX = publicKey.getW().getAffineX();
        final BigInteger affineY = publicKey.getW().getAffineY();
        final BigInteger prime = ((ECFieldFp) curve.getField()).getP();

        // Verify x and y are in range [0,p-1]
        if (affineX.compareTo(BigInteger.ZERO) < 0 ||
                affineX.compareTo(prime) >= 0 ||
                affineY.compareTo(BigInteger.ZERO) < 0 ||
                affineY.compareTo(prime) >= 0) {

            return false;
        }

        final BigInteger aCoefficient = curve.getA();
        final BigInteger bCoefficient = curve.getB();

        // Verify that y^2 == x^3 + ax + b (mod p)
        final BigInteger ySquared = affineY.modPow(BigInteger.valueOf(2), prime);
        final BigInteger xCubedPlusAXPlusB = affineX.modPow(BigInteger.valueOf(3), prime)
                .add(aCoefficient.multiply(affineX)).add(bCoefficient).mod(prime);

        if (!ySquared.equals(xCubedPlusAXPlusB)) {
            return false;
        }

        // Verify that nQ = 0, where n is the order of the curve and Q is the public key.
        // As per http://www.secg.org/sec1-v2.pdf section 3.2.2
        return publicKey.getParams().getCofactor() == 1;
    }

    private SecretKey deriveKey(byte[] sharedSecret, PublicKey senderPublic) throws NoSuchAlgorithmException {
        final List<ByteBuffer> keys = Arrays.asList(
                ByteBuffer.wrap(getPublicKey().getEncoded()),
                ByteBuffer.wrap(senderPublic.getEncoded())
        );

        // for agreement to work, both sides need to put keys into the public key in the same order
        // for simplicity sort lexicographically
        Collections.sort(keys);

        final MessageDigest hash = MessageDigest.getInstance(SECRET_KEY_HASH_ALGORITHM);
        hash.update(sharedSecret);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        final byte[] derived = hash.digest();

        return new SecretKeySpec(derived, SECRET_KEY_ALGORITHM);
    }


    public void generateKeys() {
        try {
            KeyPairGenerator    keyPairGenerator    = KeyPairGenerator.getInstance(PUBLIC_KEY_ALGORITHM);
            ECGenParameterSpec  spec                = new ECGenParameterSpec(EC_CURVE_ALGORITHM);
            keyPairGenerator.initialize(spec);

            KeyPair             keyPair             = keyPairGenerator.generateKeyPair();

            privateKey          = (ECPrivateKey) keyPair.getPrivate();
            publicKey           = (ECPublicKey) keyPair.getPublic();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            Log.wtf(TAG,"Failed to generate keys, either our algorithm is unimplemented in to the" +
                    "device, or something real bad happened...");

            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
}
