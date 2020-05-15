package wtf.declan.muzzle.data.db.converters;

import androidx.room.TypeConverter;

import wtf.declan.muzzle.message.MessageType;


/**
 * Used to convert MessageType enum to string and backwards so it can be stored within the database
 */
public class MessageTypeConverter {

    @TypeConverter
    public static MessageType toMessageType(String name) {
        if (name == null) {
            return null;
        }
        return MessageType.valueOf(name);
    }

    @TypeConverter
    public static String toName(MessageType messageType) {
        return messageType.name();
    }

}
