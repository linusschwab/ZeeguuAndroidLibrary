package ch.unibe.zeeguulibrary.Core;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.MyWords.MyWordsHeader;
import ch.unibe.zeeguulibrary.MyWords.MyWordsInfoHeader;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;

/**
 * Class to connect with the Zeeguu API
 */
public class ZeeguuConnectionManager {

    private final String URL = "https://www.zeeguu.unibe.ch/";
    private RequestQueue queue;

    private ZeeguuAccount account;
    private Activity activity;
    private String selection, selectionOutputLanguage, translation;
    private ZeeguuConnectionManagerCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuConnectionManagerCallbacks {

        void showZeeguuLoginDialog(String title, String email);
        void showZeeguuCreateAccountDialog(String message, String username, String email);

        void setTranslation(String translation);
        void highlight(String word);

        void displayErrorMessage(String error, boolean isToast);
        void displayMessage(String message);

        void notifyDataChanged(boolean myWordsChanged);
        void bookmarkWord(String bookmarkID);
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
            callback.showZeeguuLoginDialog("", "");
        else if (!account.isUserInSession())
            getSessionId(account.getEmail(), account.getPassword());
        else if (!account.isLanguageSet()) {
            getUserLanguages();
            getMyWordsFromServer();
        } else {
            getMyWordsFromServer();
        }
    }

    /**
     * Method that must be called after the activity is restored (for example on screen rotation),
     * otherwise the callbacks will still go to the old/destroyed activity!
     */
    public void onRestore(Activity activity) {
        this.activity = activity;
        account.onRestore(activity);
        callback = (ZeeguuConnectionManagerCallbacks) activity;
    }

    public void createAccountOnServer(final String username, final String email, final String password) {
        String url_create_account = URL + "add_user/" + email;

        StringRequest request = new StringRequest(Request.Method.POST,
                url_create_account, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                account.setEmail(email);
                account.setPassword(password);
                account.setSessionID(response);
                account.saveLoginInformation();
                callback.displayMessage(activity.getString(R.string.login_successful));
                getUserLanguages();
                getMyWordsFromServer();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("create_account", error.toString());
                callback.showZeeguuCreateAccountDialog(activity.getString(R.string.create_account_error_existing), username, email);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Gets a session ID which is needed to use the API
     */
    public void getSessionId(final String email, final String password) {
        if (!isNetworkAvailable())
            return; // ignore here

        String urlSessionID = URL + "session/" + email;

        StringRequest request = new StringRequest(Request.Method.POST,
                urlSessionID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                account.setEmail(email);
                account.setPassword(password);
                account.setSessionID(response);
                account.saveLoginInformation();
                callback.displayMessage(activity.getString(R.string.login_successful));
                getUserLanguages();
                getMyWordsFromServer();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.showZeeguuLoginDialog(activity.getString(R.string.login_zeeguu_error_wrong), email);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", password);
                return params;
            }
        };

        queue.add(request);
    }

    /**
     * Translates a given word or phrase from a language to another language
     *
     * @Precondition: user needs to be logged in and have a session id
     */
    public void translate(final String input, String inputLanguageCode, String outputLanguageCode) {
        if (!account.isUserLoggedIn()) {
            callback.displayErrorMessage(activity.getString(R.string.no_login), false);
            return;
        } else if (!isNetworkAvailable()) {
            callback.displayErrorMessage(activity.getString(R.string.error_no_internet_connection), false);
            return;
        } else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        } else if (!isInputValid(input))
            return; // ignore
        else if (isSameLanguage(inputLanguageCode, outputLanguageCode)) {
            callback.displayErrorMessage(activity.getString(R.string.error_language), false);
            return;
        } else if (isSameSelection(input, outputLanguageCode)) {
            if (translation != null)
                callback.setTranslation(translation);
            return;
        }

        // /translate/<from_lang_code>/<to_lang_code>
        String urlTranslation = URL + "translate/" + inputLanguageCode + "/" + outputLanguageCode +
                "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlTranslation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (response != null)
                    callback.setTranslation(response);
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

    public void bookmarkWithContext(String input, String fromLanguageCode, String translation, String toLanguageCode,
                                    final String title, final String url, final String context) {
        if (!account.isUserLoggedIn()) {
            callback.showZeeguuLoginDialog(activity.getString(R.string.error_login_first), "");
            return;
        } else if (!isNetworkAvailable()) {
            callback.displayMessage(activity.getString(R.string.error_no_internet_connection));
            return;
        } else if (!isInputValid(input) || !isInputValid(translation)) {
            callback.displayMessage(activity.getString(R.string.error_input_not_valid));
            return;
        } else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        callback.highlight(input);

        // /bookmark_with_context/<from_lang_code>/<term>/<to_lang_code>/<translation>
        String urlContribution = URL + "bookmark_with_context/" + fromLanguageCode + "/" + Uri.encode(input.trim()) + "/" +
                toLanguageCode + "/" + Uri.encode(translation) + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                callback.bookmarkWord(response);
                callback.displayMessage("Word saved to your wordlist");
                getMyWordsFromServer();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.displayMessage("Something went wrong. Please try again.");
                Log.e("bookmark_with_context", error.toString());
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
        } else if (!account.isUserInSession()) {
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
                        } catch (JSONException error) {
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

    public void setLanguageNative(final String languageNative) {
        if (languageNative.equals(account.getLanguageNative()))
            return;
        if (!account.isUserLoggedIn() || !isNetworkAvailable()) {
            return;
        } else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        String urlLanguage = URL + "native_language/" + languageNative + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST, urlLanguage,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("OK")) {
                            account.setLanguageNative(languageNative);
                            selection = "";
                        } else {
                            callback.displayMessage(activity.getString(R.string.error_language_combination));
                        }
                        // Save (or reset) language
                        account.saveLanguages();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("set_language_native", error.toString());
                // Reset language
                account.saveLanguages();
                callback.displayMessage(activity.getString(R.string.error_language_server));
            }
        });

        queue.add(request);
    }

    public void setLanguageLearning(final String languageLearning) {
        if (languageLearning.equals(account.getLanguageLearning()))
            return;
        if (!account.isUserLoggedIn() || !isNetworkAvailable()) {
            return;
        } else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        String urlLanguage = URL + "learned_language/" + languageLearning + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST, urlLanguage,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("OK")) {
                            account.setLanguageLearning(languageLearning);
                            selection = "";
                        } else {
                            callback.displayMessage(activity.getString(R.string.error_language_combination));
                        }
                        // Save (or reset) language
                        account.saveLanguages();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("set_language_learning", error.toString());
                // Reset language
                account.saveLanguages();
                callback.displayMessage(activity.getString(R.string.error_language_server));
            }
        });

        queue.add(request);
    }

    public boolean getMyWordsFromServer() {
        if (!account.isUserInSession()) {
            return false;
        } else if (!isNetworkAvailable()) {
            account.myWordsLoadFromPhone();
            return false;
        }

        String url_session_ID = URL + "bookmarks_by_day/with_context?session=" + account.getSessionID();

        JsonArrayRequest request = new JsonArrayRequest(url_session_ID, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray allBookmarks) {
                ArrayList<MyWordsHeader> myWords = account.getMyWords();
                myWords.clear();

                //ToDo: optimization that not everytime the whole list is sent
                try {
                    for (int j = 0; j < allBookmarks.length(); j++) {
                        JSONObject bookmark = allBookmarks.getJSONObject(j);
                        MyWordsHeader header = new MyWordsHeader(bookmark.getString("date"));
                        myWords.add(header);
                        JSONArray bookmarks = bookmark.getJSONArray("bookmarks");
                        String title = "";

                        for (int i = 0; i < bookmarks.length(); i++) {
                            JSONObject translation = bookmarks.getJSONObject(i);
                            //add title when a new one is
                            if (!title.equals(translation.getString("title"))) {
                                title = translation.getString("title");
                                header.addChild(new MyWordsInfoHeader(title));
                            }
                            //add word as entry to list
                            int id = translation.getInt("id");
                            String languageFromWord = translation.getString("from");
                            String languageFrom = translation.getString("from_lang");
                            String languageToWord = translation.getJSONArray("to").get(0).toString();
                            String languageTo = translation.getString("to_lang");
                            String context = translation.getString("context");

                            header.addChild(new MyWordsItem(id, languageFromWord, languageToWord, context, languageFrom, languageTo));
                        }
                    }

                    account.setMyWords(myWords);
                    //callback.displayMessage(activity.getString(R.string.successful_mywords_updated));
                } catch (JSONException error) {
                    Log.e("get_my_words", error.toString());
                    callback.notifyDataChanged(false); //To stop refreshing action
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("get_my_words", error.toString());
            }
        });

        queue.add(request);
        return true;
    }

    public void removeBookmarkFromServer(long bookmarkID) {
        if (!account.isUserInSession() || !isNetworkAvailable())
            return;

        String urlRemoveBookmark = URL + "/delete_bookmark/" + bookmarkID + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlRemoveBookmark, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (response.equals("OK")) {
                    callback.bookmarkWord("0"); //0 means that the bookmark has been deleted
                    callback.displayMessage(activity.getString(R.string.successful_bookmark_deleted));
                    getMyWordsFromServer();
                } else {
                    callback.displayErrorMessage(activity.getString(ch.unibe.R.string.error_bookmark_delete), true);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.displayErrorMessage(activity.getString(R.string.error_bookmark_delete), false);
                Log.e("remove_bookmark", error.toString());
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

    private boolean isSameSelection(String input, String outputLanguage) {
        boolean isSameSelection = input.equals(selection) && outputLanguage.equals(selectionOutputLanguage);
        selection = input;
        selectionOutputLanguage = outputLanguage;
        return isSameSelection;
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
