package wtf.declan.muzzle.view.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import wtf.declan.muzzle.Muzzle;
import wtf.declan.muzzle.R;
import wtf.declan.muzzle.cryptography.SessionCipher;
import wtf.declan.muzzle.data.db.entities.ConversationEntity;
import wtf.declan.muzzle.data.db.entities.SessionEntity;
import wtf.declan.muzzle.message.OutgoingEncryptedMessage;
import wtf.declan.muzzle.message.OutgoingKeyExchangeMessage;
import wtf.declan.muzzle.message.OutgoingTerminateSessionMessage;
import wtf.declan.muzzle.message.OutgoingTextMessage;
import wtf.declan.muzzle.recipient.Recipient;
import wtf.declan.muzzle.recipient.RecipientFactory;
import wtf.declan.muzzle.taskqueue.tasks.SendMessageTask;
import wtf.declan.muzzle.view.viewmodel.ConversationViewModel;
import wtf.declan.muzzle.view.viewmodel.ConversationViewModelFactory;

public class ConversationActivity extends AppCompatActivity {

    private final static String TAG = ConversationActivity.class.getSimpleName();

    // Intent Extras
    public static final String  RECIPIENT_NUMBER_EXTRA              = "RECIPIENT_NUMBER_EXTRA";
    public static final String  RECIPIENT_FROM_NOTIFICATION_EXTRA   = "FROM_NOTIFICATION_EXTRA";

    // Permissions
    private static final int    REQUEST_SEND_SMS            = 1;
    private static final int    REQUEST_CALL_PHONE          = 2;

    // Views
    protected EditText                messageTextBox;
    protected MenuItem                initiateSession;
    protected MenuItem                terminateSession;
    protected ConversationFragment    fragment;
    protected ProgressBar             progressBar;

    // View Model
    protected ConversationViewModel   conversationViewModel;

    // Variables
    private Recipient               recipient;
    private boolean                 fromNotification;

    // Session Variables
    private ConversationEntity      conversation;
    private SessionEntity           session;
    private boolean                 isEncrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation_activity);

        initializeIntents();
        initializeActionBar();
        initializeViews();
        initializeFragment();
        initializeViewModel();
    }


    private void initializeViewModel() {
        ConversationViewModelFactory viewModelFactory = new ConversationViewModelFactory(
                getApplication(),
                recipient.getNumber()
        );

        conversationViewModel = new ViewModelProvider(this, viewModelFactory).get(ConversationViewModel.class);
        conversationViewModel.getConversationWithMessagesEntity().observe(this, entity -> {
            if(entity != null) { // if theres a record in the database
                conversation = entity.getConversation();

                if(!conversation.isRead()) {
                    conversationViewModel.setRead(entity);
                }

                // read updates from session
                setSession(conversation.getSession());

                // Update Recycler view and scroll to bottom
                fragment.getConversationAdapter().submitList(entity.getMessages());
                fragment.getConversationView().smoothScrollToPosition(0);
            }

            progressBar.setVisibility(View.GONE);
        });
    }

    private void setSession(SessionEntity session) {
        if(this.session == null || session.getSessionDate().after(this.session.getSessionDate())) {
            this.session = session;

            Log.d(TAG, "Session has been updated");

            Log.d(TAG, "PUB: " + (session.getPublicKey() != null));
            Log.d(TAG, "PRIV: " + (session.getPrivateKey() != null));
            Log.d(TAG, "RECV: " + (session.getRecipientPublicKey() != null));
            Log.d(TAG, "DERV: " + (session.getDerivedKey() != null));

            if(session.getDerivedKey() == null && session.getRecipientPublicKey() != null) {
                Log.d(TAG, "Received a key but haven't agreed on it");
                showAcceptSessionDialog();
            } else if(session.getDerivedKey() != null) {
                Toast.makeText(this,"You are in an Encrypted Session", Toast.LENGTH_LONG).show();
                isEncrypted = true;
            } else {
                isEncrypted = false;
            }
        }

        updateMenuItems();
        updateTextBox();
    }

    private void showAcceptSessionDialog() {
        // If we've received a session request and haven't sent a public key
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.accept_encryption_title)
                .setMessage(getString(R.string.accept_encryption_message, recipient.getDisplayName()))
                .setCancelable(true)
                .setPositiveButton(getText(R.string.yes), (dialog, which) -> {
                    // Session Cipher
                    SessionCipher sessionCipher = new SessionCipher(session);

                    if(sessionCipher.getPublicKey() == null) {
                        sessionCipher.generateKeys();
                    }

                    // Perform Diffie-Hellman agreement
                    sessionCipher.doAgreement();

                    // Update database
                    conversation.setSession(sessionCipher.getEntity());
                    conversationViewModel.update(conversation);

                    // Send out public key
                    Muzzle.getInstance(this).getTaskManager().add(
                            new SendMessageTask(this, new OutgoingKeyExchangeMessage(
                                    recipient, sessionCipher.getPublicKey()
                            ))
                    );

                    isEncrypted = true;
                })
                .setNegativeButton(getText(R.string.no), (dialog, which) -> {
                    conversation.setSession(new SessionEntity());
                    conversationViewModel.update(conversation);
                })
                .create();
        alertDialog.show();
    }

    private void initializeFragment() {
        fragment = new ConversationFragment(recipient);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }

    private void initializeIntents() {
        String recipientNumber  = getIntent().getStringExtra(RECIPIENT_NUMBER_EXTRA);
        recipient               = RecipientFactory.getRecipientFromNumber(this, recipientNumber);
        fromNotification        = getIntent().getBooleanExtra(RECIPIENT_FROM_NOTIFICATION_EXTRA, false);
    }

    private void initializeActionBar() {
        // Add back arrow and set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(recipient.getDisplayName());
        }
    }
    private void initializeViews() {
        messageTextBox      = findViewById(R.id.message_text_box);
        initiateSession     = findViewById(R.id.start_encryption_button);
        terminateSession    = findViewById(R.id.end_encryption_button);
        progressBar         = findViewById(R.id.progress_bar);

        // Initialize Send Button
        ImageButton sendButton = findViewById(R.id.send_button);

        // Enable Incognito Mode based on Preferences in settings
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean incognitoMode = sharedPreferences.getBoolean("keyboard_incognito_state", true);
        if(incognitoMode) messageTextBox.setImeOptions(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);

        sendButton.setOnClickListener(v -> sendTextBoxMessage());
    }

    private void updateMenuItems() {
        if(initiateSession != null && terminateSession != null) {
            initiateSession.setVisible(!isEncrypted);
            terminateSession.setVisible(isEncrypted);
        }
    }

    private void updateTextBox() {
        if (messageTextBox != null) {
            messageTextBox.setHint(isEncrypted ? R.string.encrypted_message_hint : R.string.text_message_hint);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (fromNotification) {
            startActivity(new Intent(this, ConversationListActivity.class));
        } else {
            super.onBackPressed();
        }

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation_menu, menu);

        initiateSession = menu.findItem(R.id.start_encryption_button);
        terminateSession = menu.findItem(R.id.end_encryption_button);

        updateMenuItems();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.call_button:
                callRecipient(recipient);
                return true;
            case R.id.start_encryption_button:
                handleInitiateEncryption();
                return true;
           case R.id.end_encryption_button:
                handleTerminateEncryption();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    private void handleInitiateEncryption() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.initiate_session_title)
                .setCancelable(true)
                .setMessage(getString(R.string.initiate_session_message, recipient.getDisplayName()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Log.i(TAG, "Initiate Encryption");

                    SessionCipher sessionCipher = new SessionCipher(session);

                    if (sessionCipher.getPublicKey() == null) {
                        sessionCipher.generateKeys();
                    }

                    // Send out public key
                    Muzzle.getInstance(this).getTaskManager().add(
                            new SendMessageTask(this, new OutgoingKeyExchangeMessage(
                                    recipient, sessionCipher.getPublicKey()
                            ))
                    );

                    conversation.setSession(sessionCipher.getEntity());
                    conversationViewModel.update(conversation);

                    Toast.makeText(this, "Session initiation request sent", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no,null)
                .create();
        alert.show();
    }

    private void handleTerminateEncryption() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(R.string.end_session_title)
                .setCancelable(true)
                .setMessage(getString(R.string.end_session_message, recipient.getDisplayName()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // end encryption
                    Log.i(TAG, "Terminate Encryption");

                    Muzzle.getInstance(this).getTaskManager().add(
                            new SendMessageTask(this, new OutgoingTerminateSessionMessage(
                                    recipient
                            ))
                    );
                })
                .setNegativeButton(R.string.no, null)
                .create();

        alert.show();
    }


    @AfterPermissionGranted(REQUEST_SEND_SMS)
    private void sendTextBoxMessage() {
        Log.w(TAG, "sendTextBoxMessage()");
        String message = messageTextBox.getText().toString();
        if (message.trim().length() > 0) {
            String[] perms = { Manifest.permission.SEND_SMS };
            if (EasyPermissions.hasPermissions(getApplicationContext(), perms)) {
                // has permissions do the thing

                if(isEncrypted) {
                    Muzzle.getInstance(this).getTaskManager().add(
                            new SendMessageTask(this, new OutgoingEncryptedMessage(recipient, message))
                    );
                } else {
                    Muzzle.getInstance(this).getTaskManager().add(
                            new SendMessageTask(this, new OutgoingTextMessage(recipient, message))
                    );
                }

                Log.w(TAG, "Trying to send: " + message);
                messageTextBox.getText().clear();
            } else {
                // no permissions, request them now
                EasyPermissions.requestPermissions(
                        this,
                        "Muzzle requires the permission send SMS before continuing",
                        REQUEST_SEND_SMS,
                        perms);
            }
        } else {
            Toast.makeText(this, "No text to send", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CALL_PHONE)
    private void callRecipient(Recipient recipient) {
        String[] perms = {Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(getApplicationContext(), perms)) {
            Uri uri = Uri.fromParts("tel", recipient.getNumber(), null);
            Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
            startActivity(callIntent);
        } else {
            // no permissions, request them now
            EasyPermissions.requestPermissions(
                    this,
                    "Muzzle requires the permission call before continuing",
                    REQUEST_CALL_PHONE,
                    perms);
        }
    }

}
