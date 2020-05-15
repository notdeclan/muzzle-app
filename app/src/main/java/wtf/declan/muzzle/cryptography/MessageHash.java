package wtf.declan.muzzle.cryptography;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import wtf.declan.muzzle.message.MessageType;

/**
 * Class is used to provide a SHA1 Hash of a Message's prefix and body to be used to identify
 * incoming messages.
 *
 * Every message other than Text Message's are prefixed with the message hash.
 *
 * The theory behind this is that every message has a different identifier so that messages could
 * not be sorted and identified at a scale without additional computation
 *
 * Each message before being sent is prefixed with a base 64 hash of its message type prefix and body.
 *
 * Essentially messages come in like this (SHA1(MESSAGETYPE.PREFIX + MESSAGE BODY) + MESSAGE BODY
 *
 * The hash is SHA1 and is iterated through 500 times to make it more computationally complex.
 *
 * Although this obviously increases workloads for regular users, it prevents attackers / eavesdroppers
 * from just CTRL + F -> PREFIX and then getting all of each certain type's of messages.
 */
public class MessageHash {

    private static final String TAG = MessageHash.class.getSimpleName();

    /**
     * Although SHA1 having some collisions found in the wild, it should be cryptographically secure
     * enough for the purpose we are using it for.
     *
     * Essentially any hash algorithm could be used, and a more secure one could be used however
     * it would result in a longer prefix hash, therefore requiring more messages to be sent per text.
     */
    private static final String     HASH_ALGORITHM      = "SHA1";

    /**
     * Length of the HASH_ALGORITHM in bytes
     */
    private static final int        HASH_LENGTH         = 20;

    /**
     * How many times the hash will be rehashed
     */
    private static final int        HASH_ITERATIONS     = 500;


    /**
     *  Bit of maths to figure out how big Base64(SHA1 Hash(Prefix + Body)) will be.
     *
     *  Could be turned into hardcoded value (28) but this will automatically update
     *  if the HASH_ALGORITHM and HASH_LENGTH change
     */
    public static final int         ENCODED_LENGTH      = ((4 * HASH_LENGTH / 3) + 3) & ~3;

    /**
     * Returns a Base64 Encoded SHA1 Hash of the inputted MessageType and Body.
     *
     * See calculateHash documentation for more information.
     *
     * @param messageType:  Message Type from message
     * @param body:         Body of message
     * @return              Hash
     */
    public static String calculateHash(MessageType messageType, String body) {
        return calculateHash(messageType.getPrefix(), body);
    }


    /**
     * Returns a Base64 Encoded SHA1 Hash of the inputted PREFIX + BODY
     *
     * Representation of what it returns:
     *  String(Base64(HASH_ALGORITHM ^ HASH_ITERATIONS (prefix + body)))
     *
     * @param prefix:   Prefix of message (See wtf.declan.muzzle.message.MessageType)
     * @param body:     Body of message
     * @return          Hash
     */
    public static String calculateHash(String prefix, String body) {
        if(prefix.length() == 0) {
            return "";
        }

        try {
            final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            byte[] hash = (prefix + body).getBytes();   // initial hash is prefix + body
            for(int i = 0; i < HASH_ITERATIONS; i++) {  // hash itself again and again
                hash = digest.digest(hash);
            }

            return new String(Base64.encode(hash));     // encode to base64 and return
        } catch (NoSuchAlgorithmException e) {
            // This should never happen unless for some reason SHA1 algorithm is removed from Java's
            // MessageDigest
            Log.wtf(TAG, e);
        }

        return "";
    }
}
