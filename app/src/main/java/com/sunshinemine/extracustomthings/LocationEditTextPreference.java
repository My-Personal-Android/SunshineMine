package com.sunshinemine.extracustomthings;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.EditTextPreference;

import com.sunshinemine.R;

public class LocationEditTextPreference extends EditTextPreference {

    static final private int DEFAULT_MAX_LENGTH = 5;
    private int mMaxLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0,
                0
        );
        try{
            mMaxLength = a.getInteger(R.styleable.LocationEditTextPreference_android_maxLength,
                    DEFAULT_MAX_LENGTH);
            Log.v("Kaloo","called");
        }finally {
            a.recycle();
        }
    }

    @Override
    public void setText(String text) {
        if(text.length()<mMaxLength){
        }
        super.setText(text);
    }


}
