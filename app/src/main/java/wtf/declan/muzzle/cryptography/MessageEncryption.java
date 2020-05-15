package wtf.declan.muzzle.cryptography;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import wtf.declan.muzzle.exceptions.EncryptionException;
import wtf.declan.muzzle.exceptions.SessionException;
import wtf.declan.muzzle.message.Message;

/**
 * Class is used to provide encrypt and decrypt functionality to data sent and received by
 * encrypted text messages.
 *
 * All messages are encrypted and decrypted using AES/GCM/NoPadding with a 256 bit key that
 * is derived from a diffe-hellman elliptic curve agreement. See SessionCipher.java.
 *
 * Each message is sent with a different secure random IV or nonce to prevent patterns emerging.
 *
 * GCM is used to provide further authentication with messages, preventing any messages from being
 * tampered with.
 */
public class MessageEncryption {

    private static final String TAG = MessageEncryption.class.getSimpleName();

    private static final String ENCRYPTION_ALGORITHM    = "AES/GCM/NoPadding"; // AES 256 Galois/Counter Mode
    private static final int    GCM_TAG_LENGTH          = 128;      // in bits
    private static final int    GCM_IV_LENGTH           = 12;       // in bytes

    private SessionCipher session;

    public MessageEncryption(@NonNull SessionCipher session) {
        this.session = session;
    }

    /**
     * Encrypts the body portion of a Message object and returns the same object
     *
     * @param message:  Message to encrypt
     *
     * @throws EncryptionException: Will be thrown in the case of an error occurring due to
     * the cryptography methods used not being implemented. Currently, all methods used in the
     * encryption are included in the standard Java and Android implementation but say for example
     * the encryption methods used were to be removed or become deprecated, this exception is
     * likely to be thrown.
     *
     * This is because the exception is wrapped around the following Exceptions:
     * IOException | InvalidAlgorithmParameterException | BadPaddingException |
     * IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException
     *
     * @throws SessionException
     * A session exception will be thrown if an invalid session is attempted to be used, IE,
     * there is no derived key to be used.
     *
     * In this case, somehow the app has attempted to encrypt/decrypt a message before doAgreement
     * has been called in the SessionCipher. Either because they haven't generated there own personal
     * keys, or they haven't received a public key from the recipient.
     */
    public Message encrypt(@NonNull Message message) throws EncryptionException, SessionException {
        if(session.getDerivedKey() == null) {
            throw new SessionException("No Derived key for encryption");
        }

        byte[]      body        = message.getBody().getBytes();     // get bytes of the message
        byte[]      encrypted   = encrypt(body);                    // encrypt it
        byte[]      base64      = Base64.encode(encrypted);         // encode it so it can be sent
        String      encoded     = new String(base64);               // turn to string

        message.setBody(encoded);                                   // update message body

        return message;                                             // return updated message obj
    }

    /**
     * Decrypts the body portion of a Message object and returns the same object
     *
     * @param message:  Message to decrypt
     *
     * @throws EncryptionException: Will be thrown in the case of an error occurring due to
     * the cryptography methods used not being implemented. Currently, all methods used in the
     * encryption are included in the standard Java and Android implementation but say for example
     * the encryption methods used were to be removed or become deprecated, this exception is
     * likely to be thrown.
     *
     * This is because the exception is wrapped around the following Exceptions:
     * IOException | InvalidAlgorithmParameterException | BadPaddingException |
     * IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException
     *
     * @throws SessionException
     * A session exception will be thrown if an invalid session is attempted to be used, IE,
     * there is no derived key to be used.
     *
     * In this case, somehow the app has attempted to encrypt/decrypt a message before doAgreement
     * has been called in the SessionCipher. Either because they haven't generated there own personal
     * keys, or they haven't received a public key from the recipient.
     */
    public Message decrypt(@NonNull Message message) throws EncryptionException, SessionException {
        if(session.getDerivedKey() == null) {
            throw new SessionException("No Derived key for decryption");
        }

        byte[]    body        = message.getBody().getBytes();
        byte[]    decoded     = Base64.decode(body);
        String    decrypted   = decrypt(decoded);

        message.setBody(decrypted);

        return message;
    }

    /**
     * Encrypts supplied input bytes with the keys stored within the CipherSession used in the
     * initializer
     *
     * Utilizes AES 256 Galois/Counter Mode with a different SecureRandom generated IV for each
     * message
     *
     * @param bytes: Input bytes
     * @return      Secure IV + Output Bytes
     *
     * @throws EncryptionException: Will be thrown in the case of an error occurring due to
     * the cryptography methods used not being implemented. Currently, all methods used in the
     * encryption are included in the standard Java and Android implementation but say for example
     * the encryption methods used were to be removed or become deprecated, this exception is
     * likely to be thrown.
     *
     * This is because the exception is wrapped around the following Exceptions:
     * IOException | InvalidAlgorithmParameterException | BadPaddingException |
     * IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException
     *
     * @throws SessionException
     * A session exception will be thrown if an invalid session is attempted to be used, IE,
     * there is no derived key to be used.
     *
     * In this case, somehow the app has attempted to encrypt/decrypt a message before doAgreement
     * has been called in the SessionCipher. Either because they haven't generated there own personal
     * keys, or they haven't received a public key from the recipient.
     */
    private byte[] encrypt(byte[] bytes) throws EncryptionException, SessionException {
        try {
            Cipher              cipher  = getCipher();                          // Get the cipher instance
            byte[]              iv      = getSecureIV();                        // Generate a secure IV
            GCMParameterSpec    spec    = getParameterSpec(iv);                 // Create a GCM Specification for Cipher

            cipher.init(Cipher.ENCRYPT_MODE, session.getDerivedKey(), spec);    // Initialize the Cipher

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();   // Stream to write data
            outputStream.write(iv);                                             // write IV
            outputStream.write(cipher.doFinal(bytes));                          // encrypt and write

            return outputStream.toByteArray();                                  // Return IV and Encrypted bytes

        } catch (IOException | InvalidAlgorithmParameterException | BadPaddingException
                | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException e) {

            Log.e(TAG, "Failed to encrypt bytes");
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new EncryptionException("Failed to encrypt");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Failed to encrypt bytes due to an InvalidKeyException, there might " +
                    "be something wrong with the Key");

            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new SessionException("Attempted to encrypt with invalid keys inside CipherSession");
        }
    }

    private String decrypt(byte[] bytes) throws EncryptionException, SessionException {
        try {
            if(bytes.length < GCM_IV_LENGTH) {
                Log.e(TAG, "Bytes recieved are not long enough to even be a valid IV, " +
                        "prefixing may have messed up when handling incoming messages, " +
                        "or we're being fuzzed by an attacker...");

                throw new EncryptionException("Input bytes too short to be valid");
            }

            // Concat the IV and the Encrypted bytes from the byte array
            byte[]                iv          = Arrays.copyOfRange(bytes, 0, GCM_IV_LENGTH);
            byte[]                encrypted   = Arrays.copyOfRange(bytes, GCM_IV_LENGTH, bytes.length);

            // Get Cipher and spec instances
            Cipher                cipher      = getCipher();

            GCMParameterSpec      spec        = getParameterSpec(iv);

            // Initialize the cipher
            cipher.init(Cipher.DECRYPT_MODE, session.getDerivedKey(), spec);

            // Decrypt and return String
            return new String(cipher.doFinal(encrypted));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Failed to decrypt");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Failed to decrypt bytes due to an InvalidKeyException, there might " +
                    "be something wrong with the derived key");

            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            throw new SessionException("Attempted to decrypt with invalid keys inside CipherSession");
        }
    }

    /**
     * Provide a Cipher instance to be used during encryption/decryption.
     * @return AES/GCM/NoPadding Cipher instance
     *
     * @throws NoSuchPaddingException  This exception is thrown when a particular padding mechanism
     * is requested but is not available in the environment.
     * @throws NoSuchAlgorithmException This exception is thrown when a particular cryptographic
     * algorithm is requested but is not available in the environment.
     */
    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(ENCRYPTION_ALGORITHM);
    }

    /**
     * Function is used to return a secure Initialization vector to be used for encryption
     *
     * A secure IV (Nonce) is generated for every single message, this is due to a weakness
     * in block ciphering. When an IV is used multiple times for encryption, patterns can emerge
     * within the encrypted results. See https://miro.medium.com/max/2460/0*-DadgrNUqyjnBqVo.jpg for
     * a visual representation.
     *
     * @return 12 Byte array with random values
     */
    private byte[] getSecureIV() {
        byte[]          iv      = new byte[GCM_IV_LENGTH];
        SecureRandom    sr      = new SecureRandom();

        sr.nextBytes(iv);

        return iv;
    }


    /**
     * Function is used to construct a GCMParameterSpec for use within the Cipher with the
     * provided Initialization Vector
     *
     * @param iv: Initialization Vector to be used
     * @return GCMParameterSpec
     */
    private GCMParameterSpec getParameterSpec(byte[] iv) {
        return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    }

}
