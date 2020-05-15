package wtf.declan.muzzle.view.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.ConversationWithMessages;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;
import wtf.declan.muzzle.data.receivers.MarkReadReceiver;

public class ConversationViewModel extends AndroidViewModel {

    private final ConversationRepository                conversationRepository;
    private final LiveData<ConversationWithMessages>    conversation;

    ConversationViewModel(@NonNull Application application, @NonNull String number) {
        super(application);

        conversationRepository      = new ConversationRepository(application);
        conversation                = conversationRepository.getConversationWithMessages(number);
    }

    public LiveData<ConversationWithMessages> getConversationWithMessagesEntity() {
        return conversation;
    }

    public void update(ConversationEntity conversation) {
        conversationRepository.update(conversation);
    }

    public void setRead(ConversationWithMessages conversation) {
        Intent intent = MarkReadReceiver.buildIntent(
                getApplication().getApplicationContext(),
                conversation.getConversation().getId()
        );

        getApplication().sendBroadcast(intent);
    }
}
