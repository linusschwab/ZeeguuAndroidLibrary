package ch.unibe.zeeguulibrary.WebView;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Fragment for the Zeeguu Browser. Only works on Android >= 4.4!
 */
public class BrowserFragment extends ZeeguuWebViewFragment {

    private BrowserCallbacks callback;
    private Menu menu;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface BrowserCallbacks {
        void hideKeyboard();

        ZeeguuConnectionManager getConnectionManager();

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
            webView.loadUrl(formatUrl(url));
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
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.browser, menu);

        this.menu = menu;
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
            if (!url.contains("http"))
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
        } else if (id == R.id.action_search) {
            showUrlBar();
            //callback.showKeyboard();
            return true;
        } else if (id == android.R.id.home) {
            hideUrlBar();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUrlBar() {
        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(false);

        ActionBar actionBar = callback.getSupportActionBar();
        actionBar.setCustomView(R.layout.actionview_edittext);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        final EditText edittext = (EditText) actionBar.getCustomView().findViewById(R.id.url);
        edittext.setHint(webView.getUrl().replace("http://", "").replace("https://", "").replace("www.", ""));
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String url = edittext.getText().toString();
                    webView.loadUrl(formatUrl(url));

                    callback.hideKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    private void hideUrlBar() {
        MenuItem search = menu.findItem(R.id.action_search);
        if (search != null)
            search.setVisible(true);


        ActionBar actionBar = callback.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(webView.getTitle());
    }


    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = callback.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public void onResume() {
        super.onResume();
        callback.getSupportActionBar().setTitle(webView.getTitle());
    }

    private String formatUrl(String url) {
        String google = "http://www.google.ch/#safe=off&q=";

        if (!url.contains(""))
            return "http://zeeguu.unibe.ch";
        else if (!url.contains("."))
            return google + Uri.encode(url);
        else if (!url.contains("http"))
            return "http://" + url;

        return url;
    }
}
