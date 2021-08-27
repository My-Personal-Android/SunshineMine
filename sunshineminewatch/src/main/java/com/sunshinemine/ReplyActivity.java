package com.sunshinemine;

import androidx.appcompat.app.AppCompatActivity;

import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;

public class ReplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
    }

    private CharSequence getMessageText(Intent intent){
        Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if(bundle!=null){
            return bundle.getCharSequence(WearActivity.EXTRA_VOICE_REPLY);
        }
        return null;
    }
}