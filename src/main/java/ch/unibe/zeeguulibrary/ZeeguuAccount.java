package ch.unibe.zeeguulibrary;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.zeeguulibrary.MyWords.Item;
import ch.unibe.zeeguulibrary.MyWords.MyWordsHeader;
import ch.unibe.zeeguulibrary.MyWords.MyWordsItem;

public class ZeeguuAccount {

    private Activity activity;
    private ZeeguuAccountCallbacks callback;
    private SharedPreferences sharedPref;

    // User Information
    private String email;
    private String password;
    private String sessionID;
    private String languageNative;
    private String languageLearning;

    private String myWordsFileName = "zeeguuMyWordsTmp";
    private ArrayList<MyWordsHeader> myWords;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuAccountCallbacks {
        void notifyDataChanged();
    }

    public ZeeguuAccount(Activity activity) {
        this.activity = activity;
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        this.myWords = new ArrayList<>();

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuAccountCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuAccountCallbacks");
        }
    }

    /**
     * Save login information in preferences if they are correct (if server sent sessionID)
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
        myWordsClearOnPhone();

        // Delete variables
        email = "";
        password = "";
        sessionID = "";
        languageNative = "";
        languageLearning = "";
        myWords.clear();

        callback.notifyDataChanged();
    }

    public void switchLanguages() {
        String tmpLanguageNative = languageNative;
        languageNative = languageLearning;
        languageLearning = tmpLanguageNative;
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

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isFirstLogin() {
        if (sharedPref.getBoolean("pref_zeeguu_first_time", true)) {
            sharedPref.edit().putBoolean("pref_zeeguu_first_time", false).apply();
            return true;
        }
        return false;
    }

    // Interaction with MyWords
    public boolean isMyWordsEmpty() {
        return myWords.isEmpty();
    }

    public Item deleteWord(long id) {
        for (MyWordsHeader myWordsHeader : myWords) {
            for (int itemPosition = 0; itemPosition < myWordsHeader.getChildrenSize(); itemPosition++) {
                if (id == myWordsHeader.getChild(itemPosition).getItemId()) {
                    return myWordsHeader.removeChild(itemPosition);
                }
            }
        }
        return null;
    }

    public MyWordsItem checkMyWordsForTranslation(String input, String inputLanguage, String outputLanguage) {
        for (MyWordsHeader myWordsHeader : myWords) {
            MyWordsItem result = myWordsHeader.checkMyWordsForTranslation(input, inputLanguage, outputLanguage);
            if (result != null)
                return result;
        }
        return null;
    }

    // Loading and writing my words from and to memory, IO interface
    public void saveMyWordsOnPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            file.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            write(bufferedWriter);
            bufferedWriter.close();

            Log.d("zeeguu_myWords", "Saved words to file at location: " + file.getPath());

        } catch (IOException e) {
            Log.e("zeeguu_myWords", e.getMessage());
        }
    }

    public void write(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("" + myWords.size());
        bufferedWriter.newLine();
        for (MyWordsHeader r : myWords) {
            bufferedWriter.write(r.getName());
            bufferedWriter.newLine();
            r.write(bufferedWriter);
            bufferedWriter.flush();
        }
    }

    public void myWordsLoadFromPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            read(bufferedReader);
            bufferedReader.close();
            callback.notifyDataChanged();

            Log.d("zeeguu_myWords", "Load words from file at location: " + activity.getFilesDir().toString());

        } catch (Exception e) {
            Log.d("zeeguu_myWords", e.getMessage());
        }
    }

    public void read(BufferedReader bufferedReader) throws IOException {
        myWords.clear();

        int size = Integer.parseInt(bufferedReader.readLine());
        for (int i = 0; i < size; i++) {
            //get the name of the header group and create it
            MyWordsHeader r = new MyWordsHeader(bufferedReader.readLine().trim());
            //read all entries from the group and add it to the list
            r.read(bufferedReader);
            myWords.add(r);
        }
    }

    public void myWordsClearOnPhone() {
        try {
            File file = new File(activity.getFilesDir(), myWordsFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("");
            bufferedWriter.close();
            callback.notifyDataChanged();
        } catch (Exception e) {
            Log.e("zeeguu_myWords", "MyWords on phone could not be deleted");
        }
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
        callback.notifyDataChanged();
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

    public ArrayList<MyWordsHeader> getMyWords() {
        return myWords;
    }

    public void setMyWords(ArrayList<MyWordsHeader> myWords) {
        this.myWords = myWords;
        saveMyWordsOnPhone();
        callback.notifyDataChanged();
    }
}