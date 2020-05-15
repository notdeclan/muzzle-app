package wtf.declan.muzzle.cryptography;

/**
 * Just a wrapper for Android's Base64 methods to reduce code and prevent mistakes
 * like using the wrong flags at different points of code.
 *
 * NO_WRAP is being used to reduce the characters needed to reduce the amount of texts sent
 * per message due to length constraints in SMS.
 *
 * Technically these base64 methods could be swapped out with Bytes to HEX, and HEX to bytes,
 * but this would obviously also increase message size.
 *
 * also technically anything that transforms bytes into characters (and back) which can be sent
 * over text would work, quoted printable for example.
 */
public class Base64 {

    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input: the bytes to encode
     */
    public static byte[] encode(byte[] input) {
        return android.util.Base64.encode(input, android.util.Base64.NO_WRAP);
    }

    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * @param input: the bytes to decode
     */
    public static byte[] decode(byte[] input) {
        return android.util.Base64.decode(input, android.util.Base64.NO_WRAP);
    }

}
