package com.sunshinemine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.sunshinemine.MainActivity;
import com.sunshinemine.R;
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

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SunshineSyncAdapter";

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

         String url = extras.getString("URL");

        String result = null;
        try {
            result = makeSynchronousRequest(url);
            Log.v(LOG_TAG,result.toString());

            WeatherForecast.v(LOG_TAG,result);
            WeatherForecast.setPreference(getContext(),result);

            try {
                ArrayList<WeatherForecast> arrayList = WeatherForecast.getWeatherDataFromJson(WeatherForecast.getPreference(getContext()));
                // weatherAdapter.swapWeather(arrayList);
                long locationId = addlocation(MainActivity.getPreference(getContext()),MainActivity.getPreference(getContext()),32.5742,73.4828);
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
        int data = getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cvArray);
        Log.v("Hello ->>>>>>","Bulk ROWS  = "+data+"");
    }

    private long addlocation(String locationSetting,String cityName,double lat,double lon){
        Cursor cursor = getContext().getContentResolver().query(
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
            Uri locationUri = getContext().getContentResolver().insert(
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
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context,String url) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString("URL",url);
        ContentResolver.requestSync(getSyncAccount(context),
                "com.sunshinemine", bundle);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
//        /*
//         * Since we've created an account
//         */
//        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
//
//        /*
//         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
//         */
//        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
//
//        /*
//         * Finally, let's do a sync to get things started
//         */
//        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
