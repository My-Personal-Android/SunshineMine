package com.sunshinemine;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.sunshinemine.databinding.ActivityWearBinding;

public class WearActivity extends Activity {

    private TextView mTextView;
    private ActivityWearBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWearBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
    }
}