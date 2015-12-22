package ch.unibe.zeeguulibrary.WebView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.StringReader;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Base fragment for the Zeeguu WebView. Only works on Android >= 4.4!
 */
public class ZeeguuWebViewFragment extends Fragment {

    protected RelativeLayout translationBar;
    protected TextView translationView;
    protected ImageView bookmarkButton;
    protected WebView webView;

    protected ProgressBar progressBar;

    private String context, title, url;
    private String selection, translation;
    private boolean displayTitle = true;

    protected SharedPreferences sharedPref;

    protected ZeeguuWebViewCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuWebViewCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        ZeeguuAccount getZeeguuAccount();

        void showZeeguuLoginDialog(String title, String email);

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

    /**
     * The system calls this when it's time for the fragment to draw its user interface for the
     * first time. To draw a UI for your fragment, you must return a View from this method that
     * is the root of your fragment's layout. You can return null if the fragment does not
     * provide a UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_webview, container, false);
        translationBar = (RelativeLayout) mainView.findViewById(R.id.webview_translation_bar);
        translationView = (TextView) mainView.findViewById(R.id.webview_translation);
        bookmarkButton = (ImageView) mainView.findViewById(R.id.webview_bookmark);
        webView = (WebView) mainView.findViewById(R.id.webview_content);
        progressBar = (ProgressBar) mainView.findViewById(R.id.webview_progress_bar);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prepareWebView();
    }

    private void prepareWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (sharedPref.getBoolean("pref_browser_viewport", false)) {
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
        }
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.addJavascriptInterface(new ZeeguuWebViewInterface(getActivity()), "Android");

        // Set up the bookmark button
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractContextFromPage();
                translationBar.setVisibility(View.GONE);
            }
        });

        // Force links and redirects to open in the WebView instead of in a browser, inject css and javascript
        webView.setWebViewClient(new ZeeguuWebViewClient(getActivity(), callback, webView, displayTitle));

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);

                if (progress < 100) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                } else {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuWebViewCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement WebViewCallbacks");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void extractContextFromPage() {
        webView.evaluateJavascript("getContext();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                JsonReader reader = new JsonReader(new StringReader(value));
                try {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("term"))
                            selection = reader.nextString();
                        else if (name.equals("context"))
                            context = reader.nextString();
                        else if (name.equals("title"))
                            title = reader.nextString();
                        else if (name.equals("url"))
                            url = reader.nextString();
                        else
                            reader.skipValue();
                    }
                    reader.endObject();
                } catch (IOException e) {
                }

                submitContext();
            }
        });
    }

    public void submitContext() {
        callback.getZeeguuConnectionManager().bookmarkWithContext(selection, sharedPref.getString("pref_zeeguu_language_learning", "EN")
                , translation, sharedPref.getString("pref_zeeguu_language_native", "DE"), title, url, context);
    }

    public void setTranslation(final String translation) {
        // UI changes must be done in the main thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translationView.setText(Html.fromHtml("<h2>" + translation + "</h2>"));
            }
        });
        this.translation = translation;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void highlight(String word) {
        webView.evaluateJavascript("highlight_words_in_page([\"" + word + "\"]);", null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void unhighlight() {
        webView.evaluateJavascript("unhighlight_words_in_page();", null);
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    public boolean goBack() {
        if (webView == null)
            return true;
        else if (webView.canGoBack()) {
            webView.goBack();
            return false;
        } else
            return true;
    }

    /**
     * The system calls this method as the first indication that the user is leaving the fragment
     * (though it does not always mean the fragment is being destroyed). This is usually where you
     * should commit any changes that should be persisted beyond the current user session
     * (because the user might not come back).
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        webView.saveState(savedInstanceState);
        savedInstanceState.putString("translation", translation);
    }

    // Getters/Setters
    public RelativeLayout getTranslationBar() {
        return translationBar;
    }

    public void setTranslationBar(RelativeLayout translationBar) {
        this.translationBar = translationBar;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public String getTranslationContext() {
        return context;
    }

    public void setTranslationContext(String context) {
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void loadUrl(String url) {
        if (webView != null)
            webView.loadUrl(url);
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getTranslation() {
        return translation;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public ZeeguuWebViewCallbacks getCallback() {
        return callback;
    }

    public void setCallback(ZeeguuWebViewCallbacks callback) {
        this.callback = callback;
    }

    public void enableTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }
}
