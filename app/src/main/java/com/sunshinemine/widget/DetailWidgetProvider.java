package com.sunshinemine.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;
import com.sunshinemine.MainActivity;
import com.sunshinemine.R;
import com.sunshinemine.Utility;
import com.sunshinemine.WeatherForecastDetailsActivity;
import com.sunshinemine.sync.SunshineSyncAdapter;

public class DetailWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String location = Utility.getPreferredLocation(context);
        Log.v("Logooo","onupdate"+"");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

            views.setTextViewText(R.id.widget_location,location);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);

            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, WeatherForecastDetailsActivity.class)
                    : new Intent(context, MainActivity.class);

            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.v("Logooo","onreceive"+"");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

            onUpdate(context,appWidgetManager,appWidgetIds);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }



    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        String location = Utility.getPreferredLocation(context);
        views.setTextViewText(R.id.widget_location,location);
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, DetailWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        String location = Utility.getPreferredLocation(context);
        views.setTextViewText(R.id.widget_location,location);
        views.setRemoteAdapter(0, R.id.widget_list, new Intent(context, DetailWidgetRemoteViewsService.class));
    }
}