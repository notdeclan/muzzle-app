package wtf.declan.muzzle.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import wtf.declan.muzzle.data.db.entities.MessageEntity;

@Dao
public interface MessageDao {

    @Insert
    long insert(MessageEntity message);

    @Update
    void update(MessageEntity... messages);

    @Query("SELECT * from messages where id=:id LIMIT 1")
    MessageEntity get(long id);

    @Query("SELECT * FROM messages WHERE read=0 AND conversationId=:conversationId AND notified=0")
    List<MessageEntity> getUnreadUnNotifiedMessages(long conversationId);

    @Query("DELETE from messages")
    void deleteAll();

}
