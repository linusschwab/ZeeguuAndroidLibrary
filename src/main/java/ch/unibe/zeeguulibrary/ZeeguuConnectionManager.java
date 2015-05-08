package ch.unibe.zeeguulibrary;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.unibe.R;

/**
 *  Class to connect with the Zeeguu API
 */
public class ZeeguuConnectionManager {

    private final String URL = "https://www.zeeguu.unibe.ch/";
    private RequestQueue queue;

    private ZeeguuAccount account;
    private Activity activity;
    private String selection, translation;
    private ZeeguuConnectionManagerCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuConnectionManagerCallbacks {
        void showZeeguuLoginDialog(String title);
        void setTranslation(String translation, boolean isErrorMessage);
        void highlight(String word);
    }

    public ZeeguuConnectionManager(Activity activity) {
        this.account = new ZeeguuAccount(activity);
        this.activity = activity;

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuConnectionManagerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuConnectionManagerCallbacks");
        }

        queue = Volley.newRequestQueue(activity);

        // Load user information
        account.load();

        // Get missing information from server
        if (!account.isUserLoggedIn())
            callback.showZeeguuLoginDialog(activity.getString(R.string.login_zeeguu_title));
        else if (!account.isUserInSession())
            getSessionId(account.getEmail(), account.getPassword());
        else if (!account.isLanguageSet())
            getUserLanguages();
    }

    /**
     *  Gets a session ID which is needed to use the API
     */
    public void getSessionId(String email, String password) {
        if (!isNetworkAvailable())
            return; // ignore here

        account.setEmail(email);
        account.setPassword(password);

        String urlSessionID = URL + "session/" + email;

        StringRequest request = new StringRequest(Request.Method.POST,
                urlSessionID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                account.setSessionID(response);
                account.saveLoginInformation();
                Toast.makeText(activity, activity.getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                getUserLanguages();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                account.setEmail("");
                account.setPassword("");
                callback.showZeeguuLoginDialog("Wrong email or password");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", account.getPassword());
                return params;
            }
        };

        queue.add(request);
    }

    /**
     *  Translates a given word or phrase from a language to another language
     *
     *  @Precondition: user needs to be logged in and have a session id
     */
    public void translate(final String input, String inputLanguageCode, String outputLanguageCode) {
        if (!account.isUserLoggedIn()) {
            callback.setTranslation(activity.getString(R.string.no_login), true);
            return;
        }
        else if (!isNetworkAvailable()) {
            callback.setTranslation(activity.getString(R.string.no_internet_connection), true);
            return;
        }
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }
        else if (!isInputValid(input))
            return; // ignore
        else if (isSameLanguage(inputLanguageCode, outputLanguageCode)) {
            callback.setTranslation(activity.getString(R.string.error_language), true);
            return;
        }
        else if (isSameSelection(input)) {
            if (translation != null)
                callback.setTranslation(translation, false);
            return;
        }

        selection = input;

        // /translate/<from_lang_code>/<to_lang_code>
        String urlTranslation = URL + "translate/" + inputLanguageCode + "/" + outputLanguageCode +
                "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlTranslation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (response != null)
                    callback.setTranslation(response, false);
                translation = response;
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: handle error responses
                Log.e("translation", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("word", Uri.encode(input.trim()));
                params.put("url", "");
                params.put("context", "");

                return params;
            }
        };

        // TODO: Set tag and cancel all older translations
        queue.add(request);
    }

    public void contributeWithContext(String input, String inputLanguageCode, String translation, String translationLanguageCode,
                                      final String title, final String url, final String context) {
        if (!account.isUserLoggedIn()) {
            callback.showZeeguuLoginDialog(activity.getString(R.string.login_zeeguu_title));
            return;
        }
        else if (!isNetworkAvailable() || !isInputValid(input) || !isInputValid(translation))
            return;
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        callback.highlight(input);

        String urlContribution = URL + "contribute_with_context/" + inputLanguageCode + "/" + Uri.encode(input.trim()) + "/" +
                translationLanguageCode + "/" + Uri.encode(translation) + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(activity, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e("contribute_with_context", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("url", url);
                params.put("context", context);

                return params;
            }
        };

        queue.add(request);
    }

    private void getUserLanguages() {
        if (!account.isUserLoggedIn() || !isNetworkAvailable()) {
            return;
        }
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        String urlLanguage = URL + "learned_and_native_language" + "?session=" + account.getSessionID();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlLanguage,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            account.setLanguageNative(response.getString("native"));
                            account.setLanguageLearning(response.getString("learned"));
                            account.saveLanguages();
                        }
                        catch (JSONException error) {
                            Log.e("get_user_language", error.toString());
                        }
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("get_user_language", error.toString());
                    }
                });

        queue.add(request);
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean isInputValid(String input) {
        return !(input == null || input.trim().equals(""));
    }

    private boolean isSameSelection(String input) {
        return input.equals(selection);
    }

    private boolean isSameLanguage(String inputLanguageCode, String translationLanguageCode) {
        return inputLanguageCode.equals(translationLanguageCode);
    }

    // Getters and Setters
    public ZeeguuAccount getAccount() {
        return account;
    }

    public void setAccount(ZeeguuAccount account) {
        this.account = account;
    }
}
