package wtf.declan.muzzle.data.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;

import javax.crypto.SecretKey;

import wtf.declan.muzzle.data.db.converters.DateConverter;
import wtf.declan.muzzle.data.db.converters.ECPrivateKeyConverter;
import wtf.declan.muzzle.data.db.converters.ECPublicKeyConverter;
import wtf.declan.muzzle.data.db.converters.SecretKeyConverter;

@Entity
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    private long sessionId;

    @TypeConverters(SecretKeyConverter.class)
    private SecretKey       derivedKey;

    @TypeConverters(ECPublicKeyConverter.class)
    private ECPublicKey     publicKey;

    @TypeConverters(ECPrivateKeyConverter.class)
    private ECPrivateKey privateKey;

    @TypeConverters(ECPublicKeyConverter.class)
    private ECPublicKey     recipientPublicKey;

    @TypeConverters(DateConverter.class)
    private Date sessionDate;

    public SessionEntity() {
        this.sessionDate = new Date();
    }

    public SecretKey getDerivedKey() {
        return derivedKey;
    }

    public ECPublicKey getPublicKey() {
        return publicKey;
    }

    public ECPrivateKey getPrivateKey() {
        return privateKey;
    }

    public ECPublicKey getRecipientPublicKey() {
        return recipientPublicKey;
    }

    public void setDerivedKey(SecretKey derivedKey) {
        this.derivedKey = derivedKey;
    }

    public void setPublicKey(ECPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(ECPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setRecipientPublicKey(ECPublicKey recipientPublicKey) {
        this.recipientPublicKey = recipientPublicKey;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date updated) {
        this.sessionDate = updated;
    }
}
