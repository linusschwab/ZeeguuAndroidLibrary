package ch.unibe.zeeguulibrary.WebView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ch.unibe.zeeguulibrary.Core.Utility;

public class ZeeguuWebViewClient extends WebViewClient {

    private Activity activity;
    private ZeeguuWebViewFragment.ZeeguuWebViewCallbacks callback;
    private WebView webView;
    private boolean displayTitle;

    public ZeeguuWebViewClient(Activity activity, ZeeguuWebViewFragment.ZeeguuWebViewCallbacks callback, WebView webView, boolean displayTitle) {
        this.activity = activity;
        this.callback = callback;
        this.webView = webView;
        this.displayTitle = displayTitle;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // css
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/injectCSS.js"), null);
        String css = Utility.assetToString(activity, "css/highlight.css").replace("\n", "").replace("\r", "").trim();
        view.evaluateJavascript("injectCSS(\"" + css + "\");", null);
        // javascript
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/jquery-2.1.3.min.js"), null);
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/selectionChangeListener.js"), null);
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/extract_contribution.js"), null);
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/highlight_words.js"), null);
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/extract_context.js"), null);
        view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/text_selection.js"), null);

        callback.getZeeguuConnectionManager().getAccount().highlightMyWords();

        if (displayTitle)
            callback.getSupportActionBar().setTitle(webView.getTitle());
    }
}
