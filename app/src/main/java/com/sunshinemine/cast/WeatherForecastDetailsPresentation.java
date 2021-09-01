package com.sunshinemine.cast;

import android.app.Presentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.sunshinemine.R;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class WeatherForecastDetailsPresentation extends Presentation {

    private LinearLayout mLayout;
    private TextView mText;
    private Context mContext;

    public WeatherForecastDetailsPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the custom layout
        setContentView(R.layout.weatherforecast_details_presentation);


    }

}

