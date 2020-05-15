package wtf.declan.muzzle.data.db.converters;

import androidx.room.TypeConverter;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import wtf.declan.muzzle.cryptography.Base64;
import wtf.declan.muzzle.cryptography.SessionCipher;

/**
 * Used to convert ECPublicKey from Session Cipher into a string which can be stored in the database
 * to be later converted back
 */
public class ECPublicKeyConverter {

    @TypeConverter
    public static ECPublicKey toPublicKey(String encoded) {
        if (encoded == null) {
            return null;
        }

        try {
            KeyFactory          keyFactory  = KeyFactory.getInstance(SessionCipher.PUBLIC_KEY_ALGORITHM);
            byte[]              keyBytes    = Base64.decode(encoded.getBytes());
            X509EncodedKeySpec  keySpec     = new X509EncodedKeySpec(keyBytes);

            return (ECPublicKey) keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    @TypeConverter
    public static String toEncoded(ECPublicKey publicKey) {
        if(publicKey == null) {
            return null;
        }

        return new String(Base64.encode(publicKey.getEncoded()));
    }

}
