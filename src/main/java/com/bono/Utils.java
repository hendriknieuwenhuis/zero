package com.bono;

import java.time.Duration;

/**
 * Created by hendriknieuwenhuis on 12/02/16.
 */
public class Utils {

    private static String HTTP = "http://";
    private static String HTTPS = "https://";

    public static String loadUrl(String http) {
        String param = "";
        int httpIndex = 0;
        if (http.contains(HTTP)) {
            httpIndex = http.lastIndexOf(HTTP) + HTTP.length();
        } else if (http.contains(HTTPS)) {
            httpIndex = http.lastIndexOf(HTTPS) + HTTPS.length();
        }
        param = "soundcloud://url/" + http.substring(httpIndex);

        return param;
    }

    public static String time(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
