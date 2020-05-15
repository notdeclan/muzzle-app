package wtf.declan.muzzle.view.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.view.adapters.ConversationAdapter;


public class ConversationFragment extends Fragment {

    private static final String TAG = ConversationFragment.class.getSimpleName();

    private Recipient       recipient;
    private RecyclerView    conversationView;

    public ConversationFragment(Recipient recipient) {
        this.recipient = recipient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        initializeViews(view);
        initializeConversationAdapter();
    }

    private void initializeViews(View view) {
        conversationView = view.findViewById(R.id.conversation_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        // Reverse content so messages feed from bottom
        linearLayoutManager.setReverseLayout(true);
        conversationView.setLayoutManager(linearLayoutManager);
    }

    private void initializeConversationAdapter() {
        conversationView.setAdapter(new ConversationAdapter(recipient));
    }

    public ConversationAdapter getConversationAdapter() {
        return (ConversationAdapter) conversationView.getAdapter();
    }

    public RecyclerView getConversationView() {
        return conversationView;
    }

}
