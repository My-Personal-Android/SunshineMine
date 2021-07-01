package com.sunshinemine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public class WeatherForecast {

    private Context mContext;
    private Executor executor;
    private Handler resultHandler;
    private HttpCallBack httpCallBack;

    private static final String LOG_TAG = "WeatherForecast";

    private static final String PREFERENCE_FILE_WEATHERFORECAST = "com.sunshine";
    private static final String KEY_WEATHERFORECAST = "WeatherForecast";

    public static boolean setPreference(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_WEATHERFORECAST, value);
        return editor.commit();
    }

    public static String getPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        return settings.getString(KEY_WEATHERFORECAST, "defaultValue");
    }

    public static void ClearSharedPrefences(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    public WeatherForecast() {
    }
    public WeatherForecast(Context context, Executor executor, Handler handler ){
        this.mContext=context;
        this.executor = executor;
        this.resultHandler = handler;
    }

    public static void v(String TAG, String message) {
        int maxLogSize = 2000;
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            android.util.Log.v(TAG, message.substring(start, end));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void makeRequest(HttpCallBack httpCallBack,String url) throws IOException {
        Log.v(LOG_TAG,"httpCallRequest makeRequest");
        this.httpCallBack = httpCallBack;
        notifyFirst();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.v(LOG_TAG,"httpCallRequest doInBackground / executor ");
                String result = null;
                try {
                    result = makeSynchronousRequest(url);
                    notifyResult(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void notifyFirst() {
        Log.v(LOG_TAG,"httpCallRequest onPreExecute Done");
        try {
            httpCallBack.onPreExecute("onPreExecute Done",LOG_TAG);
        }catch (Exception e){
            e.printStackTrace();
            Log.v(LOG_TAG,e+"");
        }
    }

    private void notifyResult(String data) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                //httpCallBack.onPostExecute(result, LOG_TAG);
                Log.v(LOG_TAG,"httpCall onPostExecute Done");
                httpCallBack.onPostExecute(data, LOG_TAG);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected String makeSynchronousRequest(String url) throws IOException {
        Log.v(LOG_TAG,"httpCallRequest makeSynchronousRequest");
        String jsonResponse = "";

        URL mUrl = null;
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {

            Log.v(LOG_TAG,"httpCallRequest Post Data = "+url);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setReadTimeout(500000 /* milliseconds 8.3 min */);
            urlConnection.setConnectTimeout(550000 /* milliseconds 8.8 min */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");


            urlConnection.connect();
            //Log.v(LOG_TAG, "httpCallRequest -> Overall Message " + urlConnection.getResponseMessage()+" \t Overall Code"+urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == 200) {
                Log.v(LOG_TAG, "httpCallRequest -> Good request " + urlConnection.getResponseMessage());
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } else if (urlConnection.getResponseCode() == 400) {

                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            }else if(urlConnection.getResponseCode()==401) {

                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            } else {

                Log.v(LOG_TAG, "Not Fetched -> " + urlConnection.getResponseMessage());
                Log.v(LOG_TAG, urlConnection.getResponseCode() + "");

            }

        } catch (IOException e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


}
