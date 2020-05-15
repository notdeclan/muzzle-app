package wtf.declan.muzzle.data.db.converters;

import android.util.Log;

import androidx.room.TypeConverter;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

import wtf.declan.muzzle.cryptography.Base64;
import wtf.declan.muzzle.cryptography.SessionCipher;

/**
 * Used to convert ECPrivateKey from Session Cipher into a string which can be stored in the database
 * to be later converted back
 */
public class ECPrivateKeyConverter {

    private static final String TAG = ECPrivateKeyConverter.class.getSimpleName();

    @TypeConverter
    public static ECPrivateKey toPrivateKey(String encoded) {
        if (encoded != null) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(SessionCipher.PUBLIC_KEY_ALGORITHM);
                byte[] keyBytes = Base64.decode(encoded.getBytes());
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

                return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                Log.e(TAG, "toPrivateKey:", e);
            }
        }
        return null;
    }

    @TypeConverter
    public static String toEncoded(ECPrivateKey privateKey) {
        if(privateKey == null) {
            return null;
        }

        return new String(Base64.encode(privateKey.getEncoded()));
    }
}