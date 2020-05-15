package wtf.declan.muzzle.data.db.converters;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Room Converter used to transform Date objects to a long timestamp value which can be stored
 * safely into the database, and vice versa.
 */
public class DateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        if (date == null) {
            return null;
        }

        return date.getTime();
    }
}
