package ch.unibe.zeeguulibrary.MyWords;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public interface Item {
    // to create the ListView so that every item gets it's own view
    View getView(LayoutInflater inflater, View convertView);

    // to identify the item and get it's identification
    long getItemId();

    // to see if an item is a translation from a word we are searching
    MyWordsItem isTranslation(String languageFrom, String languageTo, String outputLanguage);

    // to see if an item belongs to this language pair, returns true if it is, false otherwise
    boolean isLanguageCombination(String languageFrom, String languageTo);

}
