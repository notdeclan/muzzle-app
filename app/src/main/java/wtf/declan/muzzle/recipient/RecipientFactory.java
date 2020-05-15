package wtf.declan.muzzle.recipient;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.LruCache;

/*
    Static class is used to handle and store Recipient objects

 */
public class RecipientFactory {

    private static final String TAG = RecipientFactory.class.getSimpleName();

    private static final LruCache<String, Recipient> cache = new LruCache<>(1000);

    public static Recipient getRecipientFromNumber(Context context, String number) {
        synchronized (cache) {
            Recipient cached = cache.get(number);

            if(cached == null) {
                cached = new Recipient(context, number);
                cache.put(number, cached);
            }

            return cached;
        }
    }

    public static Recipient getRecipientFromId(Context context, String id) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = new String[] { id };
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            cursor.close();
            return getRecipientFromNumber(context, number);
        }

        return null;
    }

}
