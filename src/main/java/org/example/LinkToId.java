package org.example;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkToId {

    public static String extractItemId(String link) throws Exception {
        URL url = new URL(link);
        String regex = "/item/(\\d+)\\.html";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url.getPath());

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new Exception("Item ID not found in the link");
        }
    }
}
