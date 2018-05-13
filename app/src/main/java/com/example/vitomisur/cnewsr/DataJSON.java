package com.example.vitomisur.cnewsr;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
// getting the data from query json url
public final class DataJSON {

    public static final String LOG_TAG = DataJSON.class.getSimpleName();

    private DataJSON() {
    }

    // getting the json data and put it in the list using the news class

    public static List<News> getJSONData(String requestUrl) {
        final int SLEEP = 2000;
        try {
            Thread.sleep(SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //creating url
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            //makes data request
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        // put the data from the url to the list (using method below)
        List<News> news = extractFeatureFromJson(jsonResponse);

        return news;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static String makeHttpRequest(URL url) throws IOException {
        final int READ_TIMEOUT = 10000;
        final int CONNECT_TIMEOUT = 15000;
        final int GOOD_RESPONSE = 200;
        String jsonResponse = "";

        //if url is null - return nothing
        if (url == null) {
            return jsonResponse;
        }
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        // try to make a http request to get the data
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            // sets the limit time for the respond
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            // request method - get the data
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // check for the valid respond
            if (urlConnection.getResponseCode() == GOOD_RESPONSE) {
                // pack the bytes of data as an input stream
                inputStream = urlConnection.getInputStream();
                // read the pack of bytes(read the respond)
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON data.", e);
        } finally {
            // if limit time passes the request will be stopped
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            // same as an input stream
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        // gets the bytes of data from the url and transform it into readable data
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            // sets the reader for the input stream with UTF-8 format
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static List<News> extractFeatureFromJson(String newsJSON) {
        final int CONTRIBUTOR = 0;
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }
        ArrayList<News> news = new ArrayList<>();
        try {
            // getting data we need from json
            JSONObject root = new JSONObject(newsJSON);
            JSONObject response = root.getJSONObject("response");
            JSONArray articlesArray = response.getJSONArray("results");
            // get all the data in array 1 by 1
            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject newsObject = articlesArray.getJSONObject(i);
                String contributor = "";
                try {
                    JSONArray contributorArray = newsObject.getJSONArray("tags");
                    if (newsObject.length() > 0) {
                        for (int j = 0; j < contributorArray.length(); j++) {
                            JSONObject contributorObject = contributorArray.getJSONObject(j);
                            if (contributorObject.has("webTitle")) {
                                contributor = contributorObject.getString("webTitle");
                            }
                        }
                    }
                    JSONObject contributorObject = contributorArray.getJSONObject(CONTRIBUTOR);
                    contributor = contributorObject.getString("webTitle");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON parsing error", e);
                }
                if (contributor != null && !contributor.isEmpty()) {
                    contributor = "by " + contributor;
                } else {
                    contributor = "by J.D.";
                }
                // add a new News class
                String section = newsObject.getString("sectionName");
                String title = newsObject.getString("webTitle");
                String time = newsObject.getString("webPublicationDate");
                String url = newsObject.getString("webUrl");
                news.add(new News(contributor, title, section, time, url));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }
        return news;
    }
}