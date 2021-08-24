package com.sunshinemine.extracustomthings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.annotation.SuppressLint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sunshinemine.R;

public class MainActivityTest extends AppCompatActivity implements View.OnClickListener {

    private AnimatedVectorDrawableCompat tickToCross, crossToTick;
    private AnimatedVectorDrawable t,c;
    private boolean isTick = true;

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);

        FloatingActionButton fabSync = findViewById(R.id.fabSync);
        FloatingActionButton fabTickCross = findViewById(R.id.fabTickCross);


        if (Build.VERSION.SDK_INT < 21 ) {
            tickToCross = (AnimatedVectorDrawableCompat) getResources().getDrawable(R.drawable.avd_tick2cross);
            crossToTick = (AnimatedVectorDrawableCompat) getResources().getDrawable(R.drawable.avd_cross2tick);

        }else{
            Toast.makeText(this,"ELse",Toast.LENGTH_SHORT).show();
            t = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_tick2cross);
            c = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.avd_cross2tick);
        }
      //  tickToCross = AnimatedVectorDrawableCompat.create(this,R.drawable.avd_tick2cross);
    //    crossToTick = AnimatedVectorDrawableCompat.create(this,R.drawable.avd_cross2tick);


        ImageView imgSettings = findViewById(R.id.imgSettings);
        ImageView imgJD = findViewById(R.id.imgJD);
        imgSettings.setOnClickListener(this);
        imgJD.setOnClickListener(this);

        AnimatedVectorDrawableCompat animatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_sync);
        fabSync.setImageDrawable(animatedVectorDrawableCompat);
        fabSync.setOnClickListener(this);
        fabTickCross.setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imgSettings:
                Animatable animatable = (Animatable) ((ImageView) view).getDrawable();
                if (animatable.isRunning())
                    animatable.stop();
                else
                    animatable.start();
                break;


            case R.id.imgJD:
                animatable = (Animatable) ((ImageView) view).getDrawable();
                if (animatable.isRunning())
                    animatable.stop();
                else
                    animatable.start();
                break;


            case R.id.fabSync:
                animatable = (Animatable) ((FloatingActionButton) view).getDrawable();
                if (animatable.isRunning())
                    animatable.stop();
                else
                    animatable.start();
                break;

            case R.id.fabTickCross:

                AnimatedVectorDrawable drawable = isTick ? t : c;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    drawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationStart(Drawable drawable) {
                            super.onAnimationStart(drawable);
                        }

                        @Override
                        public void onAnimationEnd(Drawable drawable) {
                            super.onAnimationEnd(drawable);
                        }
                    });
                }
                FloatingActionButton fab = ((FloatingActionButton) view);
                fab.setImageDrawable(drawable);
                drawable.start();
                isTick = !isTick;
                break;


        }
    }
}