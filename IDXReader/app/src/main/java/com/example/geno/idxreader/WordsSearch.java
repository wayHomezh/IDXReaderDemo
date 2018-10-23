package com.example.geno.idxreader;



import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by geno on 22/05/18.
 */

public class WordsSearch {
    private String word = "";
    private ChildCallBack childCallBack;

    public WordsSearch() {
            childCallBack = ReaderActivity.readerActivity;
    }

    public void setSendText(String sendText){
        word = sendText;
    }

    public void sendRequestWordsWithHttpURLConnection() throws UnsupportedEncodingException {
        String APPKEY = "d5c3c1cca232fcd5";
        String urlTarget = "http://api.jisuapi.com/cidian/word";


        final String netUrl = urlTarget+"?appkey="+APPKEY+"&word="+ URLEncoder.encode(word,"utf-8");

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                StringBuilder response = new StringBuilder();

                try {
                    URL url = new URL(netUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");


                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));

                    String line;

                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    parseWordsJsonWithGSON(response.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //解析json
    private void parseWordsJsonWithGSON(String jsonData) throws JSONException {
        resultData.clear();

        JSONObject jsonObject = new JSONObject(jsonData);

        String pinYin;
        String content;

        String msg = jsonObject.getString("msg");

        if ("ok".equals(msg)){
            JSONObject result = jsonObject.getJSONObject("result");
            pinYin = result.getString("pinyin");
            content = result.getString("content");
            content = HtmlUtils.filterHtml(content);

            resultData.add(pinYin);
            resultData.add(content);

        }
        else {
            resultData.add(msg);
        }

        childCallBack.call(resultData);
    }

    public void sendRequestWordWithHttpURLConnection() throws UnsupportedEncodingException {
        String APPKEY = "c3ee5f16c77ba8a5";
        String urlTarget = "http://api.jisuapi.com/zidian/word";


        final String netUrl = urlTarget+"?appkey="+APPKEY+"&word="+ URLEncoder.encode(word,"utf-8");

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                StringBuilder response = new StringBuilder();

                try {
                    URL url = new URL(netUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");


                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));

                    String line;

                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    parseWordJsonWithGSON(response.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private List<String> resultData = new ArrayList<>();

    private void parseWordJsonWithGSON(String jsonData){
        resultData.clear();

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);

            String msg = jsonObject.getString("msg");
            String pinyin;
            String content;
            JSONObject jsonObject1;

            if ("ok".equals(msg)){
                JSONObject result = jsonObject.getJSONObject("result");
                JSONArray explain = result.getJSONArray("explain");

                for(int i=0;i<explain.length();i++){
                    jsonObject1 = explain.getJSONObject(i);
                    pinyin = jsonObject1.getString("pinyin");
                    content = jsonObject1.getString("content");
                    resultData.add(pinyin);
                    resultData.add(content);
                }
            }
            else{
                resultData.add(msg);
            }
            Log.d(TAG, "parseWordJsonWithGSON:resultData "+resultData);
            childCallBack.call(resultData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
