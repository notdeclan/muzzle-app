package wtf.declan.muzzle.recipient;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

public class Recipient {

    private final static String TAG = Recipient.class.getSimpleName();

    private Context context;

    private String displayName;
    private String number;
    private TextDrawable icon;
    private Uri contactUri;
    private int color;

    public Recipient(Context context, String number) {
        this.context = context;
        this.number = number;
    }

    public String getDisplayName() {
        if(displayName == null) {
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };
            Cursor cursor = contentResolver.query(getContactUri(), projection, null, null, null);

            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndexOrThrow((ContactsContract.PhoneLookup.DISPLAY_NAME)));
                }
                cursor.close();
            }
        }

        return displayName == null ? number : displayName;
    }

    public String getNumber() {
        return number;
    }


    public TextDrawable getIcon() {
        if(icon == null) {
            icon = TextDrawable.builder().buildRound(String.valueOf(getInitial()), getColor());
        }

        return icon;
    }


    public Bitmap getIconBitmap() {
        TextDrawable drawable = icon;

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 96; // Replaced the 1 by a 96
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 96; // Replaced the 1 by a 96

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    private char getInitial() {
        return getDisplayName().charAt(0);
    }

    public int getColor() {
        if(color == 0) {
            color = ColorGenerator.MATERIAL.getColor(getInitial());
        }

        return color;
    }

    public Uri getContactUri() {
        if(contactUri == null) {
            contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        }

        return contactUri;
    }

    public Person getPerson() {
        return new Person.Builder()
                .setName(getDisplayName())
                .setUri(getContactUri().toString())
                .setIcon(IconCompat.createWithBitmap(getIconBitmap()))
                .build();

    }
}
