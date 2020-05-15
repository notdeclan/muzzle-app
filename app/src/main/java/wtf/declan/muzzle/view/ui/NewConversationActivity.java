package wtf.declan.muzzle.view.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import wtf.declan.muzzle.R;

public class NewConversationActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ContactListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_conversation_activity);

        initializeFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_conversation_menu, menu);
        // Enable search capability
        initializeSearch(menu.findItem(R.id.search_button));
        return true;
    }


    private void initializeFragment() {
        fragment = new ContactListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }



    private void initializeSearch(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        assert searchManager != null;

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(fragment != null) {
            fragment.setSearchFilter(query);
            return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return onQueryTextSubmit(newText);
    }

}
