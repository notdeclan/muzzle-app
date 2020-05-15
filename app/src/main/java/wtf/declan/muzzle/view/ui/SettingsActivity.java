package wtf.declan.muzzle.view.ui;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import wtf.declan.muzzle.R;
import wtf.declan.muzzle.data.db.Database;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference button = findPreference("messages_reset_button");
            if (button != null) {
                button.setOnPreferenceClickListener(preference -> {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Delete All Messages")
                            .setMessage("Are you sure you would like to reset the database, this will remove all previous messages and sessions.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Database.getDatabase(getContext()).reset();

                                Snackbar.make(getView(), "Database Cleared", Snackbar.LENGTH_LONG)
                                        .setAction("Close", v -> { })
                                        .show();
                            })
                            .setNegativeButton("Cancel", null);
                    alertDialogBuilder.show();


                    return true;
                });
            }
        }

    }
}