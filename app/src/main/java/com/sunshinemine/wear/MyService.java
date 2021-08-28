package com.sunshinemine.wear;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MyService extends WearableListenerService {
    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {

       for (DataEvent dataEvent:dataEventBuffer){

           if(dataEvent.getType() == DataEvent.TYPE_CHANGED){

               DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
               String path = dataEvent.getDataItem().getUri().getPath();

               if(path.equals("/step-counter")){
                   int steps = dataMap.getInt("step-count");
                   long time  = dataMap.getLong("timestamp");
               }
           }
       }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("/path/message")){

        }
    }
}