package wtf.declan.muzzle.view.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import wtf.declan.muzzle.R;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.view.adapters.ContactListAdapter;
import wtf.declan.muzzle.view.callbacks.RecipientClickCallback;

public class ContactListFragment extends Fragment implements RecipientClickCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = ContactListFragment.class.getSimpleName();

    // Loaders
    private static final int LOADER_CONTACTS = 1;

    // Permissions
    private final static int PERMISSION_CONTACTS = 1;

    // Views
    private RecyclerView        contactListView;
    private ProgressBar         progressBar;
    private ContactListAdapter  contactListAdapter;

    // Search query
    private String searchFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_fragment, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyLoader();
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
        initializeContactListAdapter();
        initializeLoader();
    }

    private void initializeViews(View view) {
        if(getActivity() != null) {
            progressBar         = getActivity().findViewById(R.id.progress_bar);
            contactListView     = view.findViewById(R.id.contact_list_view);
            contactListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        }
    }

    private void initializeContactListAdapter() {
        contactListAdapter = new ContactListAdapter(this);
        contactListView.setAdapter(contactListAdapter);
    }

    @AfterPermissionGranted(PERMISSION_CONTACTS)
    private void initializeLoader() {
        assert getActivity() != null;
        String[] perms = {Manifest.permission.READ_CONTACTS};
        if(EasyPermissions.hasPermissions(getActivity(), perms)) {
            // has permissions do the thing
            LoaderManager.getInstance(this).initLoader(LOADER_CONTACTS, null, this);
        } else {
            // no permissions, request them now
            EasyPermissions.requestPermissions(
                    this,
                    "Muzzle requires the permission to read contacts",
                    PERMISSION_CONTACTS,
                    perms);
        }
    }

    private void destroyLoader() {
        LoaderManager.getInstance(ContactListFragment.this).destroyLoader(LOADER_CONTACTS);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        assert getActivity() != null;

        String [] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };

        String      selection       = null;
        String[]    selectionArgs   = null;

        if(searchFilter != null) {
            selection       = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? ";
            selectionArgs   = new String[] { "%" + searchFilter + "%" };
        }
        return new CursorLoader(getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            getRecipients(loader.getContext(), cursor);
            progressBar.setVisibility(View.GONE);
        }
    }


    private void getRecipients(@NonNull Context context, Cursor cursor) {
        final List<Recipient> recipients = new ArrayList<>();

        while (cursor.moveToNext()) {
            String  id          = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            int     hasNumber   = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if(hasNumber > 0) {
                Recipient recipient = RecipientFactory.getRecipientFromId(context, id);
                recipients.add(recipient);
            }
        }

        cursor.close();
        contactListAdapter.submitList(recipients);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (contactListAdapter != null) {
            contactListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConversationClick(Recipient recipient) {
        Intent intent = new Intent(getContext(), ConversationActivity.class);
        intent.putExtra(ConversationActivity.RECIPIENT_NUMBER_EXTRA, recipient.getNumber());
        startActivity(intent);
    }

    void setSearchFilter(String searchFilter) {
        if(getActivity() != null) {
            this.searchFilter = searchFilter;
            LoaderManager.getInstance(getActivity()).restartLoader(LOADER_CONTACTS, null, this);
        }
    }
}
