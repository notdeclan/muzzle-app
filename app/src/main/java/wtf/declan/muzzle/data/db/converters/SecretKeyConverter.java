package wtf.declan.muzzle.data.db.converters;

import androidx.room.TypeConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import wtf.declan.muzzle.cryptography.Base64;
import wtf.declan.muzzle.cryptography.SessionCipher;

/**
 * Used to convert the derived key from the diffe helman agreement used in MessageEncryption to
 * a string and then back to SecretKey
 */
public class SecretKeyConverter {

    @TypeConverter
    public static SecretKey toPrivateKey(String encoded) {
        if (encoded == null) {
            return null;
        }

        byte[] keyBytes = Base64.decode(encoded.getBytes());

        return new SecretKeySpec(keyBytes, SessionCipher.SECRET_KEY_ALGORITHM);
    }

    @TypeConverter
    public static String toEncoded(SecretKey secretKey) {
        if(secretKey == null) {
            return null;
        }

        return new String(Base64.encode(secretKey.getEncoded()));
    }

}
