package com.sunshinemine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.sunshinemine.MainActivity;
import com.sunshinemine.R;
import com.sunshinemine.Utility;
import com.sunshinemine.WeatherForecast;
import com.sunshinemine.data.WeatherContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SunshineSyncAdapter";

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 5 ; // sec
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 137;


    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK,LOCATION_STATUS_SERVER_DOWN,LOCATION_STATUS_SERVER_INVALID,LOCATION_STATUS_UNKNOWN})
    public @interface LocationStatus{}


    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;


    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        String url = getMyUrl(getContext());

        String result = null;
        try {

            result = makeSynchronousRequest(url);

            Log.v("Kaloo",result);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String location = preferences.getString(getContext().getString(R.string.pref_city_key),getContext().getString(R.string.pref_city_default));

            long locationId = addlocation(
                    MainActivity.getPreference(getContext()),MainActivity.getPreference(getContext()),
                    Double.parseDouble(location.split("/")[0].split(",")[0]),
                    Double.parseDouble(location.split("/")[0].split(",")[1]));

            try {
                insertBulk_AfterFilterData(WeatherForecast.getWeatherDataFromJson(result), locationId);
            }catch (Exception e){
                setLocationStatus(getContext(),LOCATION_STATUS_SERVER_INVALID);
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
        }

        ContentValues[] cvArray =  new ContentValues[weatherForecastArrayList.size()];
        newList.toArray(cvArray);
        Log.v("ToArray",cvArray.length+"");
        int data = getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,cvArray);
        Log.v("Hello ->>>>>>","Bulk ROWS  = "+data+"");
        setLocationStatus(getContext(),LOCATION_STATUS_OK);
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
        String jsonResponse = null;

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
            if (urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode()<=299) {
                Log.v(LOG_TAG, "httpCallRequest -> Good request " + urlConnection.getResponseMessage());
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                setLocationStatus(getContext(),LOCATION_STATUS_OK);

            } else if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() <=499) {
                setLocationStatus(getContext(),LOCATION_STATUS_SERVER_INVALID);
                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            }
            else if (urlConnection.getResponseCode() >= 500 && urlConnection.getResponseCode() <=599) {
                setLocationStatus(getContext(),LOCATION_STATUS_SERVER_DOWN);
                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            }else {
                setLocationStatus(getContext(),LOCATION_STATUS_UNKNOWN);
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

//        if(jsonResponse == null){
//            setLocationStatus(getContext(),LOCATION_STATUS_SERVER_INVALID);
//        }else if(jsonResponse == ""){
//            setLocationStatus(getContext(),LOCATION_STATUS_SERVER_DOWN);
//        }
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
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.CONTENT_AUTHORITY), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.CONTENT_AUTHORITY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.CONTENT_AUTHORITY), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    private static String getMyUrl(Context context){

        String URL ="";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String location = preferences.getString(context.getString(R.string.pref_city_key),context.getString(R.string.pref_city_default));

        MainActivity.setPreference(context,location.split("/")[1]);

        Log.v("City Location",location.split("/")[0]);

        URL = context.getString(R.string.URL_FORMAT,location.split("/")[0].split(",")[0],location.split("/")[0].split(",")[1]);
        Log.v("City Url",URL);

        return URL;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void notifyWeather(Context context) {

        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {
            Log.v("AWAIS","Display");

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                Log.v("AWAIS","Notify");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String location = preferences.getString(context.getString(R.string.pref_city_key),context.getString(R.string.pref_city_default));

                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = location.split("/")[1];

                Log.v("AWAIS",locationQuery);
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, String.valueOf(System.currentTimeMillis()));

                Log.v("AWAIS",weatherUri.toString());
                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    Log.v("AWAIS","Move to First");
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            Utility.getArtResourceForWeatherCondition(weatherId));
                    String title = context.getString(R.string.app_name) + ": "+locationQuery.toLowerCase();

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            Utility.convertToCamelCase(desc),
                            WeatherForecast.formatHightLows(context, high,low).split("/")[0],
                            WeatherForecast.formatHightLows(context, high,low).split("/")[1]);


                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = null;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
                        notificationManager.createNotificationChannel(notificationChannel);
                        mBuilder = new NotificationCompat.Builder(context, notificationChannel.getId());
                    } else {
                        mBuilder = new NotificationCompat.Builder(context);
                    }

                    mBuilder = mBuilder
                            .setColor(resources.getColor(R.color.sunshine_light_blue))
                            .setSmallIcon(iconId)
                            .setLargeIcon(largeIcon)
                            .setContentTitle(title)
                            .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
//        }
    }

    static private void setLocationStatus(Context context, @LocationStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_location_status_key),locationStatus);
        spe.commit();
    }

}
