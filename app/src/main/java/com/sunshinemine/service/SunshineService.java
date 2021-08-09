package com.sunshinemine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.sunshinemine.HttpCallBack;
import com.sunshinemine.MainActivity;
import com.sunshinemine.WeatherForecast;
import com.sunshinemine.data.WeatherContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class SunshineService extends IntentService {

    private static final String LOG_TAG = "SunshineService";

    public SunshineService() {
        super("Sunshinemine");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(LOG_TAG,"httpCallRequest makeRequest");

        String url = intent.getStringExtra("URL");

        String result = null;
        try {
            result = makeSynchronousRequest(url);
            Log.v(LOG_TAG,result.toString());

            WeatherForecast.v(LOG_TAG,result);
            WeatherForecast.setPreference(this,result);

            try {
                ArrayList<WeatherForecast> arrayList = WeatherForecast.getWeatherDataFromJson(WeatherForecast.getPreference(this));
                // weatherAdapter.swapWeather(arrayList);
                long locationId = addlocation(MainActivity.getPreference(this),MainActivity.getPreference(this),32.5742,73.4828);
                Log.v("Hello Brother",locationId+"");
                Log.v("Hello Size",arrayList.size()+"");
                insertBulk_AfterFilterData(arrayList,locationId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void insertBulk_AfterFilterData(ArrayList<WeatherForecast> weatherForecastArrayList,long id){

        ArrayList<ContentValues> newList = new ArrayList<>();

        for(int i=0;i<weatherForecastArrayList.size();i++){

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY,id);
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DATETEXT,weatherForecastArrayList.get(i).getDt());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DEGREES,weatherForecastArrayList.get(i).getWind_deg());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_HUMIDITY,weatherForecastArrayList.get(i).getHumidity());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_PRESSURE,weatherForecastArrayList.get(i).getPressure());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,weatherForecastArrayList.get(i).getTemp().getMax());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,weatherForecastArrayList.get(i).getTemp().getMin());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,weatherForecastArrayList.get(i).getWeatherArrayList().get(0).getDescription());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,weatherForecastArrayList.get(i).getWind_speed());
            weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,weatherForecastArrayList.get(i).getWeatherArrayList().get(0).getId());

            Log.v("Waloo",weatherValues.toString());
            newList.add(weatherValues);
//            Uri locationUri = getContentResolver().insert(
//                    WeatherContract.WeatherEntry.CONTENT_URI,weatherValues
//            );
//            Log.v("Waoo",ContentUris.parseId(locationUri)+"");
        }

        ContentValues[] cvArray =  new ContentValues[weatherForecastArrayList.size()];
        newList.toArray(cvArray);
        Log.v("ToArray",cvArray.length+"");
        int data = getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cvArray);
        Log.v("Hello ->>>>>>","Bulk ROWS  = "+data+"");
    }

    private long addlocation(String locationSetting,String cityName,double lat,double lon){
        Cursor cursor = getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null
        );
        if(cursor.moveToFirst()){
            Log.v("Hello","Founded Record in the database");
            int id= cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return  cursor.getLong(id);
        }else{
            Log.v("Hello","Not Founded Record in the database");

            ContentValues contentValues = new ContentValues();
            contentValues.put(WeatherContract.LocationEntry.COULUMN_CITY_NAME,cityName);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,locationSetting);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LAT,lat);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LONG,lon);
            Uri locationUri = getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,contentValues
            );

            return ContentUris.parseId(locationUri);
        }
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


    static public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            Intent myIntent = new Intent(context, SunshineService.class);
            myIntent.putExtra("URL", intent.getStringExtra("URL"));
            context.startService(myIntent);

        }
    }
}
