package wtf.declan.muzzle.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.ConversationWithMessages;

@Dao
public interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(ConversationEntity conversation);

    @Update
    int update(ConversationEntity... conversation);

    @Transaction
    @Query("SELECT * FROM conversations WHERE number=:number LIMIT 1")
    ConversationEntity getConversation(String number);

    @Transaction
    @Query("SELECT * FROM conversations WHERE id=:id LIMIT 1")
    ConversationEntity getConversation(long id);

    @Transaction
    @Query("SELECT * FROM conversations WHERE number=:number LIMIT 1")
    LiveData<ConversationWithMessages> getConversationWithMessages(String number);

    @Transaction
    @Query("SELECT * from conversations WHERE visible=1 ORDER by UPDATED DESC")
    LiveData<List<ConversationWithMessages>> getVisibleConversations();

    @Query("DELETE from conversations")
    void deleteAll();

}
