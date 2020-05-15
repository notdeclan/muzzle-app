package wtf.declan.muzzle.data.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.Collections;
import java.util.List;

public class ConversationWithMessages {

    @Embedded
    private ConversationEntity conversation;

    @Relation(
            parentColumn = "id",
            entityColumn = "conversationId"
    )
    private List<MessageEntity> messages;

    public ConversationEntity getConversation() {
        return conversation;
    }

    public void setConversation(ConversationEntity conversation) {
        this.conversation = conversation;
    }

    public List<MessageEntity> getMessages() {
        Collections.sort(messages);
        return messages;
    }

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
    }
}