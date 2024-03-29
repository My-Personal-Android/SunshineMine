package com.sunshinemine.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import com.sunshinemine.MainActivity;
import com.sunshinemine.R;
import com.sunshinemine.Utility;
import com.sunshinemine.data.WeatherContract;

public class TodayWidgetIntentService extends IntentService {

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;

    private WallpaperManager wallpaperManager ;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onHandleIntent(Intent intent) {

        wallpaperManager = WallpaperManager.getInstance(this);
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        // Get today's data from the ContentProvider
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, String.valueOf(System.currentTimeMillis()));
        Cursor data = getContentResolver().query(
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                WeatherContract.WeatherEntry.COULUMN_DATETEXT + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        double minTemp = data.getDouble(INDEX_MIN_TEMP);

        String formattedMaxTemperature = Utility.formatTemperatureInCelsius(this, maxTemp);
        String formattedMinTemperature = Utility.formatTemperatureInCelsius(this, minTemp);

        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;

            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_today_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_today;
            } else {
                layoutId = R.layout.widget_today_small;
            }

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }

            Color backgroudImageColor = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM).getPrimaryColor();

            int invertedRed = 255 ;
            int invertedGreen = 255;
            int invertedBlue = 255;
            if(Math.round(backgroudImageColor.red()) == 1 && Math.round(backgroudImageColor.green()) == 1 && Math.round(backgroudImageColor.blue()) == 1){
                 invertedRed = 0;
                 invertedGreen = 0;
                 invertedBlue = 0;
            }
            views.setTextViewText(R.id.widget_location,location);
            views.setInt(R.id.widget_location,"setTextColor",Color.rgb(invertedRed,invertedGreen,invertedBlue));
            views.setTextViewText(R.id.widget_description, Utility.convertToCamelCase(description));
            views.setInt(R.id.widget_description,"setTextColor",Color.rgb(invertedRed,invertedGreen,invertedBlue));
            views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
            views.setInt(R.id.widget_high_temperature,"setTextColor",Color.rgb(invertedRed,invertedGreen,invertedBlue));
            views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);
            views.setInt(R.id.widget_low_temperature,"setTextColor",Color.rgb(invertedRed,invertedGreen,invertedBlue));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }

}
