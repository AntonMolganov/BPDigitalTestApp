package com.bpdigital.testapp;

import android.net.Uri;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Anton on 08.11.2015.
 */
public class BingProvider {
    private final static String BING_URL = "https://api.datamarket.azure.com/Bing/Search/v1/Image";
    private final static String BING_PASSWORD = "";
    private final static String BING_RESULTS = "50";


//    https://api.datamarket.azure.com/Bing/Search/v1/Image?Query=%27Xbox%27&$format=json


    public static ArrayList<BingResult> makeQuery(String query){
        ArrayList<BingResult> result = new ArrayList<>();

        try {

            String bingUrl = BING_URL + "?Query=%27" + Uri.encode(query) + "%27&$top=" + BING_RESULTS + "&$format=json";
            byte[] accountKeyBytes = Base64.encode((":"+BING_PASSWORD).getBytes(), Base64.DEFAULT);
            String accountKeyEnc = new String(accountKeyBytes);
            URL url = new URL(bingUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
            if (urlConnection.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) sb.append(line + "\n");
                JSONObject jo = new JSONObject(sb.toString());
                JSONArray ja = jo.getJSONObject("d").getJSONArray("results");
                for (int i = 0; i < ja.length(); i++){
                    jo = ja.getJSONObject(i);
                    String imageUrl = jo.getString("MediaUrl");
                    String thumbnailUrl = jo.getJSONObject("Thumbnail").getString("MediaUrl");
                    result.add(new BingResult(thumbnailUrl,imageUrl));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }




}
