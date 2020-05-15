package wtf.declan.muzzle.view.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.ConversationWithMessages;
import wtf.declan.muzzle.data.db.repositories.ConversationRepository;

public class ConversationListViewModel extends AndroidViewModel {

    private ConversationRepository                          conversationRepository;
    private LiveData<List<ConversationWithMessages>>        conversationListData;

    public ConversationListViewModel(@NonNull Application application) {
        super(application);
        conversationRepository  = new ConversationRepository(application);
        conversationListData    = conversationRepository.getVisibleConversations();
    }

    public LiveData<List<ConversationWithMessages>> getConversations() {
        return conversationListData;
    }

    public void setQuery(String query) {
        // TODO: Make this actually work (switch to MutableLiveData instead of LiveData)
//        if(query.length() > 0) {
//            Log.w("ViewModel", query);
//            conversationListData = conversationRepository.queryVisibleConversations(query);
//        } else {
//        conversationListData = conversationRepository.getVisibleConversations();
//        }
    }

    public void update(ConversationEntity entity) {
        conversationRepository.update(entity);
    }
}
