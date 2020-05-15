package wtf.declan.muzzle.view.ui;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.view.adapters.ConversationListAdapter;
import wtf.declan.muzzle.view.callbacks.RecipientClickCallback;
import wtf.declan.muzzle.view.callbacks.RecipientSwipeCallback;
import wtf.declan.muzzle.view.helpers.ConversationListTouchHelper;
import wtf.declan.muzzle.view.viewmodel.ConversationListViewModel;

public class ConversationListFragment extends Fragment implements RecipientClickCallback, RecipientSwipeCallback {

    private final static String TAG = ConversationListFragment.class.getSimpleName();

    private final static int PERMISSION_READ_SMS_CONTACTS = 1;

    private RecyclerView conversationThreadView;
    private ProgressBar progressBar;

    private ConversationListAdapter conversationThreadAdapter;

    private ConversationListViewModel conversationViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        conversationViewModel = new ViewModelProvider(this).get(ConversationListViewModel.class);
        conversationViewModel.getConversations().observe(getViewLifecycleOwner(), conversation -> {
            getConversationThreadAdapter().submitList(conversation);
            if (conversation.size() == 0){
                Snackbar.make(container, getString(R.string.no_messages), Snackbar.LENGTH_LONG)
                        .setAction("Close", v -> { })
                        .show();
            }

            progressBar.setVisibility(View.GONE);
        });

        return inflater.inflate(R.layout.conversation_list_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();


        if(isNotDefaultApplication()) {
            requestDefaultApplication();
        }

        getConversationThreadAdapter().notifyDataSetChanged();
    }

    private void requestDefaultApplication() {
        RoleManager roleManager     = Objects.requireNonNull(getActivity()).getSystemService(RoleManager.class);
        Intent      role            = Objects.requireNonNull(roleManager).createRequestRoleIntent(RoleManager.ROLE_SMS);

        startActivityForResult(role, 2);
    }

    private boolean isNotDefaultApplication() {
        RoleManager roleManager = Objects.requireNonNull(getActivity()).getSystemService(RoleManager.class);
        return !(roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_SMS));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        initializeViews(view);
        initializeConversationThreadAdapter();
        initializeLoader();
    }

    private void initializeViews(View view) {
        assert getActivity() != null;

        progressBar = getActivity().findViewById(R.id.progress_bar);
        conversationThreadView = view.findViewById(R.id.conversation_thread_view);
        conversationThreadView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ConversationListTouchHelper(this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(conversationThreadView);
    }

    private void initializeConversationThreadAdapter() {
        conversationThreadAdapter = new ConversationListAdapter(getContext(),this);
        conversationThreadView.setAdapter(conversationThreadAdapter);
    }


    @AfterPermissionGranted(PERMISSION_READ_SMS_CONTACTS)
    private void initializeLoader() {
        assert getActivity() != null;

        if(isNotDefaultApplication()) {
            requestDefaultApplication();
            return;
        }

        String[] perms = {Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS};
        if(EasyPermissions.hasPermissions(getActivity(), perms)) {
            // has permissions do the thing
//            LoaderManager.getInstance(this).initLoader(SMS_LOADER_ID, null, this);
        } else {
            // no permissions, request them now
            EasyPermissions.requestPermissions(
                    this,
                    "Muzzle requires the permission to read contacts and sms",
                    PERMISSION_READ_SMS_CONTACTS,
                    perms);
        }
    }

    private ConversationListAdapter getConversationThreadAdapter() {
        return conversationThreadAdapter;
    }

    @Override
    public void onConversationClick(Recipient recipient) {
        Intent intent = new Intent(getContext(), ConversationActivity.class);
        intent.putExtra(ConversationActivity.RECIPIENT_NUMBER_EXTRA, recipient.getNumber());
        startActivity(intent);
    }

    @Override
    public void onConversationSwiped(int adapterPosition, ConversationEntity entity) {
        getConversationThreadAdapter().remove(adapterPosition); // remove the swipe view from recycler
        entity.setVisible(false);
        conversationViewModel.update(entity);                   // update database
    }

}
