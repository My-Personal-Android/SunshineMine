package com.sunshinemine;

import static com.sunshinemine.Utility.convertToCamelCase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sunshinemine.data.WeatherContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WeatherForecastDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    public static String DATA_KEY_EXTRA = "Data";
    public static final String LOCATION_KEY = "location";

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATETEXT,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COULUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COULUMN_PRESSURE,
            WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COULUMN_DEGREES,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING
    };

    public static final int COL_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_ID = 9;
    public static final int COL_LOCATION_SETTING = 10;

    private  String intent_data = null;

    public TextView date_textview;
    public TextView forecast_textview;
    public TextView high_textview;
    public TextView low_textview;
    public TextView detail_day_textview;
    public ImageView detail_icon;
    public TextView detail_humidity_textview;
    public TextView detail_pressure_textview;
    public TextView detail_wind_textview;
    public TextView selected_city_textview;

    private String mLocation;
    private String pic_key =null;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = new Bundle();
            bundle.putString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,intent.getExtras().getString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC));
            bundle.putString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,intent.getExtras().getString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING));
            showDialogtoAlert(bundle);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        LoaderManager.getInstance(this).initLoader(DETAIL_LOADER,null,this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        WeatherForecast weatherForecast = getIntent().getParcelableExtra(DATA_KEY_EXTRA);
        Log.v("Helo",weatherForecast.toString());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pic_key = prefs.getString(this.getString(R.string.pref_pics_key),this.getString(R.string.pref_pics_default));
        this.pic_key=pic_key;

        selected_city_textview=findViewById(R.id.selected_city_textview);
        date_textview =findViewById(R.id.date_textview);
        forecast_textview =findViewById(R.id.forecast_textview);
        high_textview =findViewById(R.id.high_textview);
        low_textview =findViewById(R.id.low_textview);
        detail_day_textview = findViewById(R.id.detail_day_textview);
        detail_icon = findViewById(R.id.detail_icon);
        detail_humidity_textview= findViewById(R.id.detail_humidity_textview);
        detail_pressure_textview = findViewById(R.id.detail_pressure_textview);
        detail_wind_textview = findViewById(R.id.detail_wind_textview);

        selected_city_textview.setText(Utility.convertToCamelCase(MainActivity.getPreference(this).toLowerCase()));

    }

    private Intent creatShareForecastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,intent_data + " # SUNSHINE MINE by Awais Mansha");
        return intent;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(shareActionProvider!=null){
            shareActionProvider.setShareIntent(creatShareForecastIntent());
        }else{
            Log.v("Looo","Share Action Provide is null");
        }

        return true;
    }

    public void showDialogtoAlert(Bundle bundle){
        new AlertDialog.Builder(this)
                .setTitle(" Weather Alert")
                .setMessage("Heads up : " +Utility.convertToCamelCase(bundle.getString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC))+ " in " + Utility.convertToCamelCase(bundle.getString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING)) + " ..! ")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }})
                .show();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        WeatherForecast weatherForecast = getIntent().getParcelableExtra(DATA_KEY_EXTRA);

        mLocation = MainActivity.getPreference(this);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation,String.valueOf(weatherForecast.getDt()));

        return new CursorLoader(
                this,
                weatherUri,
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if(data!=null && data.moveToFirst()){

            Log.v("Hooo",data.getString(0)+" - "+ data.getString(1)+" - "+ data.getString(2)+" - "+ data.getString(3)+" - "+ data.getString(4)+" - "+ data.getString(5)+" - "+ data.getString(6)+" - "+ data.getString(7)+" - "+ data.getString(8)+" - "+ data.getString(9)+" - "+ data.getString(10));

            detail_day_textview.setText(getDateText_ExceptDate(Long.parseLong(data.getString(COL_WEATHER_DATE))));
            detail_day_textview.setContentDescription(getDateText_ExceptDate(Long.parseLong(data.getString(COL_WEATHER_DATE))));
            date_textview.setText(WeatherForecast.getReadableDateString(Long.parseLong(data.getString(COL_WEATHER_DATE))) + "");
            date_textview.setContentDescription(WeatherForecast.getReadableDateString(Long.parseLong(data.getString(COL_WEATHER_DATE))) + "");
            forecast_textview.setText(convertToCamelCase(data.getString(COL_WEATHER_DESC)+""));
            forecast_textview.setContentDescription(convertToCamelCase(data.getString(COL_WEATHER_DESC)+""));
            high_textview.setText(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[0]+"\u00B0");
            high_textview.setContentDescription(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[0]+"\u00B0");
            low_textview.setText(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[1]+"\u00B0");
            low_textview.setContentDescription(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[1]+"\u00B0");
            if(pic_key.equals("image")){
                Glide.with(this)
                        .load(Utility.getImageUrlForWeatherCondition(data.getInt(COL_WEATHER_ID)))
                        .error(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_ID)))
                        .circleCrop()
                        .into(detail_icon);
            }else {
                Glide.with(this)
                        .load(Utility.getArtUrlForWeatherCondition(this, data.getInt(COL_WEATHER_ID)))
                        .error(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_ID)))
                        .into(detail_icon);
            }
            detail_icon.setContentDescription(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_ID))+"");

            detail_humidity_textview.setText(data.getString(COL_WEATHER_HUMIDITY)+"%");
            detail_humidity_textview.setContentDescription("Humidity : "+data.getString(COL_WEATHER_HUMIDITY)+"%");
            detail_pressure_textview.setText(data.getString(COL_WEATHER_PRESSURE)+" hPa");
            detail_pressure_textview.setContentDescription("Pressure : "+data.getString(COL_WEATHER_PRESSURE)+" hPa");
            detail_wind_textview.setText(Utility.getFormattedWind(this,Float.parseFloat(data.getString(COL_WEATHER_WIND_SPEED)),Float.parseFloat(data.getString(COL_WEATHER_DEGREES)))+"");
            detail_wind_textview.setContentDescription("Wind : "+Utility.getFormattedWind(this,Float.parseFloat(data.getString(COL_WEATHER_WIND_SPEED)),Float.parseFloat(data.getString(COL_WEATHER_DEGREES)))+"");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private String getDateText_ExceptDate(long mydate){

        Date date = new Date(mydate * 1000);

        DateFormat df = new SimpleDateFormat("dd:MM:yy");
        String newDate = df.format(date);

        //Calendar cal = Calendar.getInstance();
        String current_day = df.format(new Date());
        Log.v("Date",newDate + " - "+current_day);


        Calendar cal  = Calendar.getInstance();

        //adding a day
        cal.add(Calendar.DATE, +1);

        if(newDate.equals(current_day)){
            return "Today";
        }else if(newDate.equals(df.format(new Date(cal.getTimeInMillis()).getTime()))){
            return "Tomorrow";
        }

        return "On" ;
    }
}