package com.letbyte.contact.control;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by hp on 7/5/2015.
 */
public class Http {

    private static final String URL = "http://letbyte.com/api/contact/search/";
    private static final int mConnectTimeout = 10000;
    private static final int mReadTimeout = 30000;
    private static final String mGet = "GET";
    private static final String mPost = "POST";

    private Http() {
    }

    public static JSONObject onHttp(final JSONObject json) throws IOException, JSONException {

        boolean isPost = json != null && json.length() != 0;

        if (json != null) {
            Control.log(json.toString());
        }

        HttpURLConnection http = null;
        JSONObject jsonResponse = null;
        try {

            http = (HttpURLConnection) new URL(URL).openConnection();
            http.setConnectTimeout(mConnectTimeout);
            http.setReadTimeout(mReadTimeout);

            if (isPost) {

                http.setDoOutput(true);
                http.setRequestMethod(mPost);
                http.setFixedLengthStreamingMode(json.toString().getBytes().length);
                http.setRequestProperty("Content-Type", "application/json; charset=utf-8");



                PrintWriter writer = new PrintWriter(http.getOutputStream());
                writer.print(json.toString());
                writer.close();
            }

            int responseCode = http.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) throw new UnknownError();

            jsonResponse = readStream(http.getInputStream());

            if (jsonResponse == null) throw new NullPointerException();

        } finally {
            if (http != null) {
                http.disconnect();
            }
        }

        return jsonResponse;
    }

    private static JSONObject readStream(final InputStream is) throws JSONException {
        String data = new Scanner(is).useDelimiter("\\A").next();
        if (data == null || data.isEmpty()) return null;

        Control.log("RRR >>> " + data);

        return new JSONObject(data);
    }
}
