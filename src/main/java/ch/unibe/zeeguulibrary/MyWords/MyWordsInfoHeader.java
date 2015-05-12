package ch.unibe.zeeguulibrary.MyWords;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class MyWordsInfoHeader implements Item {
    private final String name;

    public MyWordsInfoHeader(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

        holder.header_title.setText(name);

        return convertView;
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public MyWordsItem isTranslation(String input, String inputLanguage, String outputLanguage) {
        return null; //because a MyWordsHeader cannot be a translation of a word
    }

    static class ViewHolder {
        TextView header_title;
    }

}
