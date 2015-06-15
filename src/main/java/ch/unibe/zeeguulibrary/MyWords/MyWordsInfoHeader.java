package ch.unibe.zeeguulibrary.MyWords;

import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ch.unibe.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class MyWordsInfoHeader implements Item {
    private final String name;
    private final String url;
    private boolean clicked;

    public MyWordsInfoHeader(String name, String url) {
        this.name = name;
        this.url = url;
        clicked = false;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView != null && convertView.getTag().getClass() == ViewHolder.class) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.mywords_info_header, null);
            holder = new ViewHolder();

            holder.header_title = (TextView) convertView.findViewById(R.id.txtInfoHeader);

            convertView.setTag(holder);
        }

        if (url.equals("")) {
            holder.header_title.setText(name);
        } else {
            holder.header_title.setText(Html.fromHtml(("<u>" + url + "</u>")));
            holder.header_title.setTextColor(Color.parseColor(clicked ? "#800080" : "#0000FF"));
        }

        return convertView;
    }

    // Interfaces
    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public MyWordsItem isTranslation(String languageFrom, String languageTo, String outputLanguage) {
        return null; //because a MyWordsHeader cannot be a translation of a word
    }

    @Override
    public boolean isLanguageCombination(String languageFrom, String languageTo) {
        return false; //because a MyWordsHeader is never a language combination
    }

    // Getter und Setter
    static class ViewHolder {
        TextView header_title;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setClicked() {
        clicked = true;
    }
}
