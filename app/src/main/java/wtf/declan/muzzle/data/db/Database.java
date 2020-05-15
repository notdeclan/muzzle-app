package wtf.declan.muzzle.data.db;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wtf.declan.muzzle.data.db.dao.ConversationDao;
import wtf.declan.muzzle.data.db.dao.MessageDao;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.MessageEntity;
import wtf.declan.muzzle.data.db.entities.SessionEntity;

@androidx.room.Database(
        entities = {
                ConversationEntity.class,
                MessageEntity.class,
                SessionEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class Database extends RoomDatabase {

    private static final String TAG = Database.class.getSimpleName();

    public abstract MessageDao messageDao();
    public abstract ConversationDao threadDao();

    private static int NUMBER_OF_THREADS = 4;

    private static volatile Database INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static Database getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (Database.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Database.class,
                            "Conversations"
                    ).build();
                }
            }
        }

        return INSTANCE;
    }

    public void reset() {
        databaseWriteExecutor.execute(() -> {
            messageDao().deleteAll();
            threadDao().deleteAll();
            Log.d(TAG, "Reset all rows");
        });
    }
}
