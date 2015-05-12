package ch.unibe.zeeguulibrary.MyWords;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Zeeguu Application
 * Created by Pascal on 22/01/15.
 */
public class MyWordsItem implements Item {

    private long id;
    private String languageFromWord;
    private String languageFrom;
    private String languageToWord;
    private String languageTo;
    private String context;


    public MyWordsItem(long id, String languageFromWord, String languageToWord, String context, String languageFrom, String languageTo) {
        this.id = id;
        this.languageFromWord = languageFromWord;
        this.languageToWord = languageToWord;
        this.context = context;
        this.languageFrom = languageFrom;
        this.languageTo = languageTo;
    }


    public String getLanguageFrom() {
        return languageFrom;
    }

    public String getLanguageTo() {
        return languageTo;
    }

    public String getContext() {
        return context;
    }

    public String getLanguageFromWord() {
        return languageFromWord;
    }

    public void setLanguageFromWord(String languageFromWord) {
        this.languageFromWord = languageFromWord;
    }

    public String getLanguageToWord() {
        return languageToWord;
    }

    public void setLanguageToWord(String languageToWord) {
        this.languageToWord = languageToWord;
    }

    public void setLanguageFrom(String languageFrom) {
        this.languageFrom = languageFrom;
    }

    public void setLanguageTo(String languageTo) {
        this.languageTo = languageTo;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        ViewHolder holder;

        if (convertView != null && convertView.getTag().getClass() == ViewHolder.class) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.mywords_item, null);

            holder = new ViewHolder();

            holder.languageFromWord = (TextView) convertView.findViewById(R.id.mywords_language_from_word);
            holder.languageToWord = (TextView) convertView.findViewById(R.id.mywords_language_to_word);
            holder.context = (TextView) convertView.findViewById(R.id.mywords_context);
            holder.languageFromFlag = (ImageView) convertView.findViewById(R.id.flag_language_from);
            holder.languageToFlag = (ImageView) convertView.findViewById(R.id.flag_language_to);

            convertView.setTag(holder);
        }

        holder.languageFromWord.setText(languageFromWord);
        holder.languageToWord.setText(languageToWord);

        //if context, write it into the textview, if not, don't show the textview
        if (!context.equals("")) {
            holder.context.setVisibility(View.VISIBLE);
            holder.context.setText(context);
        } else
            holder.context.setVisibility(View.GONE);

        if (languageFrom != null)
            setFlag(holder.languageFromFlag, languageFrom);
        if (languageTo != null)
            setFlag(holder.languageToFlag, languageTo);

        return convertView;
    }

    public void setFlag(ImageView flag, String language) {
        switch (language) {
            case "en":
                flag.setImageResource(R.drawable.flag_uk);
                break;
            case "de":
                flag.setImageResource(R.drawable.flag_german);
                break;
            case "fr":
                flag.setImageResource(R.drawable.flag_france);
                break;
            case "it":
                flag.setImageResource(R.drawable.flag_italy);
                break;
        }
    }

    @Override
    public long getItemId() {
        return id;
    }

    //// to see if an item is a translation from a word we are searching ////

    public MyWordsItem isTranslation(String input, String fromLanguage, String toLanguage) {
        if (fromLanguage.equals(this.languageFrom) && toLanguage.equals(this.languageTo)) {
            if (input.equals(languageFromWord))
                return this;
        } else if (fromLanguage.equals(this.languageTo) && toLanguage.equals(this.languageFrom)) {
            if (input.equals(languageToWord))
                return this;
        }
        return null;
    }

    //// View holder for the list elements so that they can be reused ////

    static class ViewHolder {
        TextView languageFromWord;
        TextView languageToWord;
        TextView context;

        ImageView languageFromFlag;
        ImageView languageToFlag;
    }

}

