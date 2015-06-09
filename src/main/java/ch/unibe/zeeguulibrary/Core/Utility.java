package ch.unibe.zeeguulibrary.Core;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Utility {

    /**
     *  Converts a file with text/code into a string,
     *  for example a Javascript file
     */
    public static String assetToString(Activity activity, String path) {
        try {
            InputStream is = activity.getAssets().open(path);
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        catch (IOException e){
            Log.e("IOException", e.getMessage());
            return "";
        }
    }

    /**
     *  Unescapes a string that contains standard Java escape sequences.
     *  (source: https://gist.github.com/uklimaschewski/6741769)
     */
    public static String unescapeString(String string) {

        StringBuilder sb = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == string.length() - 1) ? '\\' : string
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                            && string.charAt(i + 1) <= '7') {
                        code += string.charAt(i + 1);
                        i++;
                        if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                                && string.charAt(i + 1) <= '7') {
                            code += string.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= string.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + string.charAt(i + 2) + string.charAt(i + 3)
                                        + string.charAt(i + 4) + string.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
