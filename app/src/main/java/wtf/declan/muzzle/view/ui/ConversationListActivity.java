package wtf.declan.muzzle.view.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import wtf.declan.muzzle.R;

public class ConversationListActivity extends AppCompatActivity {

    private final static String TAG = ConversationListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);

        initializeFab();
        initializeFragment();
    }

    private void initializeFragment() {
        ConversationListFragment fragment = new ConversationListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }

    private void initializeFab() {
        FloatingActionButton fab = findViewById(R.id.new_message_fab);
        fab.setOnClickListener((views) -> {
            startActivity(new Intent(getBaseContext(), NewConversationActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener((item) -> {
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
            return true;
        });

        return true;
    }


}
