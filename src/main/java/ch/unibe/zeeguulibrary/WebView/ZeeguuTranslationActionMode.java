package ch.unibe.zeeguulibrary.WebView;

import android.app.Activity;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.unibe.R;

/**
 *  Action mode callback to display the translation of selected text and add bookmarks.
 */
public class ZeeguuTranslationActionMode implements ActionMode.Callback {

    private ZeeguuWebViewFragment webViewFragment;
    private Activity activity;

    public ZeeguuTranslationActionMode(ZeeguuWebViewFragment webViewFragment) {
        this.webViewFragment = webViewFragment;
        activity = webViewFragment.getActivity();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        // Remove the default menu items (select all, copy, paste, search)
        menu.clear();

        // Remove menu items individually:
        // menu.removeItem(android.R.id.[id_of_item_to_remove])

        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.translation, menu);

        return true;
    }

    // Called when action mode is first created. The menu supplied
    // will be used to generate action buttons for the action mode
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Show translation bar
        webViewFragment.getTranslationBar().setVisibility(View.VISIBLE);
        // Display login dialog if not logged in
        if (!webViewFragment.getCallback().getZeeguuAccount().isUserLoggedIn())
            webViewFragment.getCallback().showZeeguuLoginDialog(activity.getString(R.string.error_login_first), "");
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_bookmark) {
            webViewFragment.extractContextFromPage();
            actionMode.finish(); // Action picked, so close the CAB
            return true;
        }
        if (id == R.id.action_unhighlight) {
            webViewFragment.unhighlight();
            actionMode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        webViewFragment.getTranslationBar().setVisibility(View.GONE);
        webViewFragment.getTranslationBar().setText("");
    }
}