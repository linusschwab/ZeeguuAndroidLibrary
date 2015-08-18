package ch.unibe.zeeguulibrary.WebView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

public class ZeeguuWebViewInterface {
    private Activity context;
    private SharedPreferences sharedPref;
    private ZeeguuWebViewInterfaceCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuWebViewInterfaceCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        ZeeguuWebViewFragment getWebViewFragment();
    }

    /**
     *  Instantiate the interface and set the context
     */
    public ZeeguuWebViewInterface(Activity context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuWebViewInterfaceCallbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement WebViewInterfaceCallbacks");
        }
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void updateTranslation(String selection) {
        callback.getZeeguuConnectionManager().translate(selection, sharedPref.getString("pref_zeeguu_language_learning", "EN"),
                sharedPref.getString("pref_zeeguu_language_native", "DE"));
    }

    // Method to debug selection
    @JavascriptInterface
    public void updateText(String selection) {
        callback.getWebViewFragment().setTranslation(selection);
    }
}
