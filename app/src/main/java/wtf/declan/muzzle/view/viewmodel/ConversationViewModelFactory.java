package wtf.declan.muzzle.view.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ConversationViewModelFactory implements ViewModelProvider.Factory {

    private Application     application;
    private String          recipientNumber;

    public ConversationViewModelFactory(@NonNull Application application, String recipientNumber) {
        this.application        = application;
        this.recipientNumber    = recipientNumber;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel > T create(@NonNull Class<T> modelClass) {
        return (T) new ConversationViewModel(application, recipientNumber);
    }

}
