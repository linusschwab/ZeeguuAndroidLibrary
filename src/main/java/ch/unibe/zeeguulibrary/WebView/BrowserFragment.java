package ch.unibe.zeeguulibrary.WebView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Fragment for the Zeeguu Browser. Only works on Android >= 4.4!
 */
public class BrowserFragment extends ZeeguuWebViewFragment {

    private BrowserCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface BrowserCallbacks {
        void hideKeyboard();

        ZeeguuConnectionManager getConnectionManager();

        //NavigationDrawerFragment getNavigationDrawerFragment();
        ActionBar getSupportActionBar();
    }

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set custom action bar layout
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            String url = sharedPref.getString("pref_browser_homepage", "http://zeeguu.unibe.ch");
            if (url.equals(""))
                webView.loadUrl("http://zeeguu.unibe.ch");
            else if (!url.contains("http://"))
                webView.loadUrl("http://" + url);
            else
                webView.loadUrl(url);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (BrowserCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement BrowserCallbacks");
        }
    }

    // Add action view
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //if (!callback.getNavigationDrawerFragment().isDrawerOpen()) {
        menu.clear();
        inflater.inflate(R.menu.browser, menu);

        SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String url) {
                // Perform action on key press
                String google = "http://www.google.ch/#safe=off&q=";

                if (!url.contains("."))
                    webView.loadUrl(google + Uri.encode(url));
                else if (!url.contains("http://"))
                    webView.loadUrl("http://" + url);
                else
                    webView.loadUrl(url);

                callback.hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }

        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            webView.reload();
            return true;
        } else if (id == R.id.action_back) {
            if (webView.canGoBack())
                webView.goBack();
            return true;
        } else if (id == R.id.action_home) {
            String url = callback.getConnectionManager().getAccount().getHomepage();
            if (!url.contains("http://"))
                url = "http://" + url;
            webView.loadUrl(url);
            return true;
        } else if (id == R.id.action_forward) {
            if (webView.canGoForward())
                webView.goForward();
            return true;
        } else if (id == R.id.action_unhighlight) {
            unhighlight();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = callback.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }
}
