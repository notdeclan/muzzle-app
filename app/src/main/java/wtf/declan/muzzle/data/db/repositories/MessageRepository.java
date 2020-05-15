package wtf.declan.muzzle.data.db.repositories;

import android.content.Context;

import androidx.annotation.UiThread;

import java.util.List;

import wtf.declan.muzzle.data.db.Database;
import wtf.declan.muzzle.data.db.dao.MessageDao;
import wtf.declan.muzzle.data.db.entities.MessageEntity;

public class MessageRepository {

    private final MessageDao    messageDao;

    public MessageRepository(Context context) {
        Database database = Database.getDatabase(context);
        this.messageDao = database.messageDao();
    }

    public List<MessageEntity> getUnreadUnNotifiedMessages(long conversationId) {
        return messageDao.getUnreadUnNotifiedMessages(conversationId);
    }

    public MessageEntity getMessage(long messageId) {
        return messageDao.get(messageId);
    }

    @UiThread
    public void update(MessageEntity message) {
        Database.databaseWriteExecutor.execute(() -> {
            messageDao.update(message);
        });
    }

}
