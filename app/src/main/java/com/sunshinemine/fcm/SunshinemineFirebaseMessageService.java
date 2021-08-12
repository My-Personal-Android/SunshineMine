package com.sunshinemine.fcm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sunshinemine.MainActivity;
import com.sunshinemine.R;
import com.sunshinemine.Utility;
import com.sunshinemine.data.WeatherContract;

import java.util.Map;

public class SunshinemineFirebaseMessageService extends FirebaseMessagingService {

    private static final String JSON_KEY_WEATHER_DESC = WeatherContract.WeatherEntry.COULUMN_SHORT_DESC;
    private static final String JSON_KEY_LOCATION = WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING;


    private static final int NOTIFICATION_MAX_CHARACTERS = 30;
    private static String LOG_TAG = SunshinemineFirebaseMessageService.class.getSimpleName();

    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // this is called when app is running and to notify to the user

        Log.v("Kalool", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.

        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {
            Log.v("Kalool", "Message data payload: " + data);

            // Send a notification that you got a new message
            sendNotification(data);
         //   insertSquawk(data);

        }
    }



    /**
     * Create and show a simple notification containing the received FCM message
     *
     * @param data Map which has the message data in it
     */
    private void sendNotification(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Create the pending intent to launch the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String weather = data.get(JSON_KEY_WEATHER_DESC);
        String location = data.get(JSON_KEY_LOCATION);

        // If the message is longer than the max number of characters we want in our
        // notification, truncate it and add the unicode character for ellipsis
        if (weather.length() > NOTIFICATION_MAX_CHARACTERS) {
            weather = weather.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }

        builder = builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.black))
                .setContentTitle(location)
                .setContentText(weather)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        notificationManager.notify(0, builder.build());

        Intent intent1 = new Intent("MyData");
        intent1.putExtra(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC, weather);
        intent1.putExtra(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,location);
        broadcaster.sendBroadcast(intent1);
    }

}
