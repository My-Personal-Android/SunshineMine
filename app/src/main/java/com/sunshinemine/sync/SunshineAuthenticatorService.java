package com.sunshinemine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.sunshinemine.sync.SunshineAuthenticator;

public class SunshineAuthenticatorService extends Service {

    private SunshineAuthenticator mAuthenticator;


    @Override
    public void onCreate() {
        mAuthenticator = new SunshineAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
