package wtf.declan.muzzle.data.db.repositories;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import java.util.Date;
import java.util.List;

import wtf.declan.muzzle.data.db.Database;
import wtf.declan.muzzle.data.db.dao.ConversationDao;
import wtf.declan.muzzle.data.db.dao.MessageDao;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.ConversationWithMessages;
import wtf.declan.muzzle.data.db.entities.MessageEntity;

public class ConversationRepository {

    private static final String TAG = ConversationRepository.class.getSimpleName();

    private final MessageDao        messageDao;
    private final ConversationDao   conversationDao;


    public ConversationRepository(Context context) {
        Database    database            = Database.getDatabase(context);
                    conversationDao     = database.threadDao();
                    messageDao          = database.messageDao();
    }

    @Transaction
    public Pair<Long, Long> storeMessage(String number, MessageEntity message) {
        ConversationEntity conversation = getConversation(number);
        conversation.setVisible(true);
        conversation.setUpdated(message.getDate());

        message.setConversationId(conversation.getId());
        if(!message.isInbox()) {    // if we sent the message
            message.setRead(true);  // we've obviously read the message
        } else {                    // if we received the message
            conversation.setRead(false); // we haven't read it
        }

        long messageId = messageDao.insert(message);

        conversationDao.update(conversation);

        return Pair.create(conversation.getId(), messageId);
    }

    @UiThread
    public LiveData<List<ConversationWithMessages>> getVisibleConversations() {
        return conversationDao.getVisibleConversations();
    }

    @UiThread
    public LiveData<ConversationWithMessages> getConversationWithMessages(String number) {
        Database.databaseWriteExecutor.execute(()-> {
            insertConversation(number);
        });

        return conversationDao.getConversationWithMessages(number);
    }

    @WorkerThread
    public ConversationEntity getConversation(String number) {
        insertConversation(number); // This will fail if it is already in there
        return conversationDao.getConversation(number);
    }

    @WorkerThread
    public ConversationEntity getConversation(long id) {
        return conversationDao.getConversation(id);
    }

    @WorkerThread
    private void insertConversation(String number) {
        if(conversationDao.insert(new ConversationEntity(number, false, false)) != -1) {
            // as the dao will return the id of the insert, and will ignore if a constraint fails
            // it will return -1. therefore if we get an ID, we have created a new conversation
            Log.d(TAG, "Created new conversation for: " + number);
        }
    }

    @WorkerThread
    public void updateWorkerThread(ConversationEntity conversationEntity) {
        conversationEntity.setUpdated(new Date());
        conversationDao.update(conversationEntity);
    }

    @UiThread
    public void update(ConversationEntity conversation) {
        Database.databaseWriteExecutor.execute(()-> {
            updateWorkerThread(conversation);
        });
    }

}
