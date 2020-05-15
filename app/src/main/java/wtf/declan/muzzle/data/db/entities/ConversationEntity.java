package wtf.declan.muzzle.data.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import wtf.declan.muzzle.data.db.converters.DateConverter;

@Entity(
        tableName = "conversations",
        indices = {
                @Index(value = {"number"}, unique = true)
        }
)
public class ConversationEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String number;

    private boolean visible;

    @Embedded
    private SessionEntity session;

    private boolean read;

    @NonNull
    @TypeConverters(DateConverter.class)
    private Date updated;

    public ConversationEntity(@NonNull String number, boolean visible, boolean read) {
        this.number = number;
        this.visible = visible;
        this.read = read;
        this.updated = new Date();
        this.session = new SessionEntity();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getNumber() {
        return number;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @NonNull
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(@NonNull Date date) {
        this.updated = date;
    }

    public SessionEntity getSession() {
        return session;
    }

    public void setSession(SessionEntity session) {
        this.session = session;
    }

}
