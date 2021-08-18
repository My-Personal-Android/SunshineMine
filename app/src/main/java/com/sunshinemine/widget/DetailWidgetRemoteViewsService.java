package com.sunshinemine.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.sunshinemine.R;
import com.sunshinemine.Utility;
import com.sunshinemine.WeatherForecast;
import com.sunshinemine.WeatherForecastDetailsActivity;
import com.sunshinemine.data.WeatherContract;

import java.util.concurrent.ExecutionException;

public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATETEXT,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP
    };
    // these indices must match the projection
    static final int INDEX_WEATHER_ID = 0;
    static final int INDEX_WEATHER_DATE = 1;
    static final int INDEX_WEATHER_CONDITION_ID = 2;
    static final int INDEX_WEATHER_DESC = 3;
    static final int INDEX_WEATHER_MAX_TEMP = 4;
    static final int INDEX_WEATHER_MIN_TEMP = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri weatherForLocationUri = WeatherContract.WeatherEntry
                        .buildWeatherLocationWithStartDate(location, String.valueOf(System.currentTimeMillis()));
                data = getContentResolver().query(weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        WeatherContract.WeatherEntry.COULUMN_DATETEXT + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
                int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);

                Bitmap weatherArtImage = null;
                if ( !Utility.usingLocalGraphics(DetailWidgetRemoteViewsService.this) ) {
                    String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
                            DetailWidgetRemoteViewsService.this, weatherId);
                    try {
                        weatherArtImage = Glide.with(DetailWidgetRemoteViewsService.this)
                                .asBitmap()
                                .load(weatherArtResourceUrl)
                                .error(weatherArtResourceId)
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(LOG_TAG, "Error retrieving large icon from " + weatherArtResourceUrl, e);
                    }
                }

                String description = data.getString(INDEX_WEATHER_DESC);
                long dateInMillis = data.getLong(INDEX_WEATHER_DATE);
                String formattedDate = WeatherForecast.getReadableDateString(dateInMillis);
                double maxTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
                double minTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);

                String formattedMaxTemperature =
                        Utility.formatTemperatureInCelsius(DetailWidgetRemoteViewsService.this, maxTemp);
                String formattedMinTemperature =
                        Utility.formatTemperatureInCelsius(DetailWidgetRemoteViewsService.this, minTemp);

                if (weatherArtImage != null) {
                    views.setImageViewBitmap(R.id.widget_icon, weatherArtImage);
                } else {
                    views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }

                views.setTextViewText(R.id.widget_date, formattedDate);
                views.setTextViewText(R.id.widget_description, description);
                views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
                views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);


                WeatherForecast weatherForecast = new WeatherForecast();
                weatherForecast.setDt(data.getLong(INDEX_WEATHER_DATE));

                final Intent intent = new Intent(DetailWidgetRemoteViewsService.this, WeatherForecastDetailsActivity.class);
                intent.putExtra(WeatherForecastDetailsActivity.DATA_KEY_EXTRA, (Parcelable) weatherForecast);

                views.setOnClickFillInIntent(R.id.widget_list_item, intent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_WEATHER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}