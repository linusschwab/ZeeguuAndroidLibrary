package ch.unibe.scg.zeeguulibrary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ZeeguuAccount {

    private SharedPreferences sharedPref;

    // User Information
    private String email;
    private String password;
    private String sessionID;
    private String languageNative;
    private String languageLearning;

    public ZeeguuAccount(Activity activity) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    /**
     *  Save login information in preferences if they are correct (if server sent sessionID)
     */
    public void saveLoginInformation() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pref_zeeguu_username", email);
        editor.putString("pref_zeeguu_password", password);
        editor.putString("pref_zeeguu_session_id", sessionID);
        editor.apply();
    }

    public void saveLanguages() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pref_zeeguu_language_native", languageNative);
        editor.putString("pref_zeeguu_language_learning", languageLearning);
        editor.apply();
    }

    public void load() {
        email = sharedPref.getString("pref_zeeguu_username", "");
        password = sharedPref.getString("pref_zeeguu_password", "");
        sessionID = sharedPref.getString("pref_zeeguu_session_id", "");
        languageNative = sharedPref.getString("pref_zeeguu_language_native", "");
        languageLearning = sharedPref.getString("pref_zeeguu_language_learning", "");
    }

    public void logout() {
        // Delete preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pref_zeeguu_username", "");
        editor.putString("pref_zeeguu_password", "");
        editor.putString("pref_zeeguu_session_id", "");
        editor.putString("pref_zeeguu_language_native", "");
        editor.putString("pref_zeeguu_language_learning", "");
        editor.apply();

        // Delete variables
        email = "";
        password = "";
        sessionID = "";
        languageNative = "";
        languageLearning = "";
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isUserLoggedIn() {
        return !(email == null || email.equals("")) && !(password == null || password.equals(""));
    }

    public boolean isUserInSession() {
        return !(sessionID == null || sessionID.equals(""));
    }

    public boolean isLanguageSet() {
        return !(languageNative == null || languageNative.equals("")) && !(languageLearning == null || languageLearning.equals(""));
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getLanguageNative() {
        return languageNative;
    }

    public void setLanguageNative(String languageNative) {
        this.languageNative = languageNative;
    }

    public String getLanguageLearning() {
        return languageLearning;
    }

    public void setLanguageLearning(String languageLearning) {
        this.languageLearning = languageLearning;
    }
}
