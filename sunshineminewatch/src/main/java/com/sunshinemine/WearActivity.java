package com.sunshinemine;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.OnApplyWindowInsetsListener;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.sunshinemine.databinding.ActivityWearBinding;

import java.io.ByteArrayOutputStream;

public class WearActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;
    private ActivityWearBinding binding;

    private GoogleApiClient mGoogleApiClient;

    public static final int NOTIFICATION_ID = 1;

    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wear);

        mTextView = findViewById(R.id.text);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        setAmbientEnabled();


    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        mTextView.setText("Ambient Mode");
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(15.0f);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        mTextView.setText("Interactive Mode");
        mTextView.setTextColor(Color.RED);
        mTextView.setTextSize(40.0f);
    }

    public void goToReplyActivity(View view){
        Intent intent = new Intent(this,ReplyActivity.class);
        startActivity(intent);
    }

    // Notification for reply by GOOGLE but does nnot work due to EMULATOR WATCH
//    @RequiresApi(api = Build.VERSION_CO
//    DES.O)
//    public void sendNotification (View view){
//
//        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
//        bigTextStyle.setBigContentTitle("Page 2")
//                .bigText("iugkjbjkkjretg" +
//                        "rthrth" +
//                        "rtherth" +
//                        "erther" +
//                        "th" +
//                        "erth" +
//                        "rthert" +
//                        "her" +
//                        "thrtkj;erng;kegrhwjk;ehgk;we;kgnewnrgkl'" +
//                        "erthknkjerng;kjnwergkl'" +
//                        "'tng'kwenr'ghnwe'ornhg'owenrhg'lnwe'lgnw4" +
//                        "e'gnw'ekrng'lewrng'lwenrg'leqnr" +
//                        "'lg" +
//                        "etnh" +
//                        "wernhtglweknthkwenrtg" +
//                        "nhgwretnhowernhglnwrtjhknow" +
//                        "w;kjrtnhk;jwrenhtk;j w4'knh'owenrhg" +
//                        "kjrg;knw'kjerjhn;kwerg" +
//                        "'wrtehk;wjertnhg'kwnrh'le" +
//                        "we'rgn'klwernhg'lkwrtnhw" +
//                        "bh");
//
//        String replyLabel = "Reply Now!";
//        String[] replyChoices = getResources().getStringArray(R.array.reply_choices);
//        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
//                .setLabel(replyLabel)
//                .setChoices(replyChoices)
//                .build();
//
//        Intent replyIntent = new Intent(this,ReplyActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,replyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Action action  = new NotificationCompat.Action.Builder
//                (R.drawable.ic_muzei,"Reply Now!",pendingIntent)
//                .addRemoteInput(remoteInput)
//                .build();
//        String channelID = "my_channel_01";
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//        NotificationChannel mChannel = new NotificationChannel(channelID, "Awais App",NotificationManager.IMPORTANCE_HIGH);
//
//
//        String location = "Mandi Bahauddin";
//
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
//        Uri geoUri = Uri.parse("geo:0,0?q="+Uri.encode(location));
//
//        mapIntent.setData(geoUri);
//        PendingIntent mapPendingIntent  = PendingIntent.getActivity(this,0,mapIntent,0);
//
//        String title = "Title: Awais";
//        String description = "Android Developer";
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
//                .setSmallIcon(R.drawable.ic_muzei)
//                .setContentTitle(title)
//                .setContentText(description)
//                .setSubText("Page 1")
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .extend(new NotificationCompat.WearableExtender().addAction(action))
//                .setStyle(bigTextStyle)
//                .setAutoCancel(true);
//
//
//        notificationManager.createNotificationChannel(mChannel);
//
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendNotification (View view){

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();


        // The id of the channel.
        String channelID = "my_channel_01";

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        NotificationChannel mChannel = new NotificationChannel(channelID, "Awais App", NotificationManager.IMPORTANCE_HIGH);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/minarepakistan"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String location = "Mandi Bahauddin";

        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoUri = Uri.parse("geo:0,0?q="+Uri.encode(location));

        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent  = PendingIntent.getActivity(this,0,mapIntent,0);

        String title = "Title: Awais";
        String description = "Android Developer";

        bigTextStyle.bigText(title);
        bigTextStyle.bigText(description);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_muzei)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_muzei,
                        "Show in Map",mapPendingIntent)
                .setStyle(bigTextStyle)
                .setAutoCancel(true);


        notificationManager.createNotificationChannel(mChannel);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap){
        final ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return Asset.createFromBytes(byteArrayOutputStream.toByteArray());
    }
    public void sendStepCount(int steps,long timestamp){

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/step-counter");

        putDataMapRequest.getDataMap().putInt("step-count",steps);
        putDataMapRequest.getDataMap().putLong("timestamp",timestamp);

        Asset asset = createAssetFromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_muzei));
        putDataMapRequest.getDataMap().putAsset("profileImage",asset);

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if(!dataItemResult.getStatus().isSuccess()){

                        }else{

                        }
                    }
                });
    }

    public void sendMessage(){

        NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for(Node node :nodesResult.getNodes()){

            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient,node.getId(),"/path/message"
                    ,new byte[0]).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                    if(!sendMessageResult.getStatus().isSuccess()){

                    }
                }
            });
        }

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}