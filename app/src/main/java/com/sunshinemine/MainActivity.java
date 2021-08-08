package com.sunshinemine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

import com.sunshinemine.data.WeatherContract;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HttpCallBack, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "MainActivity";

    private String mLocation;
    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME+"."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATETEXT,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID
    };
    public static final int COL_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_ID = 6;

    String url = "https://api.openweathermap.org/data/2.5/onecall?";
    String lattitude = "lat=32.5742";
    String longitude = "lon=73.4828";
    String unit = "units=metric";
    String appid= "appid=8c7175339095430fdf24c1cac276d9d5";

    String my_url = url + lattitude + "&" +longitude + "&" + unit + "&" + appid;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private ArrayList<String> arrayList_WeatherForecast;
    private WeatherForecast weatherForecast = null;

    private RecyclerView recyclerview_forecast;
    private WeatherAdapter weatherAdapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER,null,this);

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);
        recyclerview_forecast.setHasFixedSize(true);
        recyclerview_forecast.setLayoutManager(new LinearLayoutManager(this));

        weatherAdapter = new WeatherAdapter(this, new ArrayList<>());
        recyclerview_forecast.setAdapter(weatherAdapter);
        recyclerview_forecast.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecastmenu, menu);
        return true;
    }
    //Android Activity Lifecycle Method
   // Called when a panel's menu is opened by the user.
    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        MenuItem mnuLogOut = menu.findItem(R.id.action_refresh);

        //set the menu options depending on login status

        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {

            case R.id.action_refresh:
            {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String location = preferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
                Log.v("Looo",location);
                weatherForecast = new WeatherForecast(this,executorService,mainThreadHandler);
                try {
                    weatherForecast.makeRequest(this,my_url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_map:
                openPreferredLocationOnMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void openPreferredLocationOnMap(){

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=37.423156,-122.084917 ( Mandi bahauddin)"));
        startActivity(intent);
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String location = preferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
//        Log.v("Looo",location);
//
//        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
//                .appendQueryParameter("q",location).build();
//        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//        intent.setData(geoLocation);
//
//        if(intent.resolveActivity(getPackageManager())!=null){
//           startActivity(intent);
//        }else{
//            Log.v("Looo","Could Not call location");
//        }

    }
    @Override
    public void onPreExecute(String result, String caller) {

        Log.v(LOG_TAG,result + ' ' + caller);
    }

    @Override
    public void onPostExecute(String result, String caller) {

      //  Log.v(LOG_TAG,result + ' ' + caller);
        WeatherForecast.v(LOG_TAG,result);
        WeatherForecast.setPreference(this,result);

        try {
            ArrayList<WeatherForecast> arrayList = WeatherForecast.getWeatherDataFromJson(WeatherForecast.getPreference(this));
           // weatherAdapter.swapWeather(arrayList);
            long locationId = addlocation("0546","Mandi Bahauddin",32.5742,73.4828);
            Log.v("Hello Brother",locationId+"");
            Log.v("Hello Size",arrayList.size()+"");
            insertBulk(arrayList,locationId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertBulk(ArrayList<WeatherForecast> weatherForecastArrayList,long id){

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


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        // Karachi, Pakistan time is 10:00 hours ahead Chicago, United States
        long ten_Hours_in_miliseconds = 36000000;
        String startDate = String.valueOf(new Date(Calendar.getInstance().getTimeInMillis() - ten_Hours_in_miliseconds).getTime());
        Log.v("Mera",startDate);

        String sortOrder = WeatherContract.WeatherEntry.COULUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                "0546", startDate);

        CursorLoader cursorLoader =  new CursorLoader(this,
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.v("Mera",data.getCount()+"");
        if(data!=null && data.moveToFirst()) {
            Log.v("Mera",data.getCount()+"");
            ArrayList<WeatherForecast> arrayList =new ArrayList<>();
            do {
                Log.v("Hola", data.getInt(COL_WEATHER_ID) + " - " + data.getString(COL_WEATHER_DATE) + " - " + data.getString(COL_WEATHER_DESC) + " - " +
                        data.getString(COL_WEATHER_MAX_TEMP) + " - " + data.getString(COL_WEATHER_MIN_TEMP) + " - " + data.getString(COL_LOCATION_SETTING));

                WeatherForecast weatherForecast = new WeatherForecast();
                weatherForecast.setDt(Long.parseLong(data.getString(COL_WEATHER_DATE)));

                ArrayList<WeatherForecast.weather> weathers = new ArrayList<>();
                WeatherForecast.weather weather = new WeatherForecast.weather();
                weather.setMain(data.getString(COL_WEATHER_DESC));
                weather.setId(data.getInt(COL_WEATHER_ID));
                weathers.add(weather);
                weatherForecast.setWeatherArrayList(weathers);

                WeatherForecast.Temp temp = new WeatherForecast.Temp();
                temp.setMax(Double.parseDouble(data.getString(COL_WEATHER_MAX_TEMP)));
                temp.setMin(Double.parseDouble(data.getString(COL_WEATHER_MIN_TEMP)));
                weatherForecast.setTemp(temp);

                arrayList.add(weatherForecast);
            }
            while (data.moveToNext());
            weatherAdapter.swapWeather(arrayList);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        weatherAdapter.swapWeather(null);
    }
}