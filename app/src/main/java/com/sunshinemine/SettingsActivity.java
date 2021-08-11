package com.sunshinemine;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.sunshinemine.data.WeatherContract;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_framelayout, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
        getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,null);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);

            EditTextPreference preference = findPreference(getContext().getString(R.string.pref_location_key));
            preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER); // set only numbers allowed to input
                    editText.selectAll(); // select all text
                    int maxLength = 5;
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                    Log.v("Kaloo",editText.getText().toString());
                }
            });
//            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                  //  Log.v("Kaloo",newValue;
////                    editText.setInputType(InputType.TYPE_CLASS_NUMBER); // set only numbers allowed to input
////                    editText.selectAll(); // select all text
////                    int maxLength = 5;
////                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
////                    Log.v("Kaloo",editText.getText().toString());
//                    return false;
//                }
//            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,null);
        NavUtils.navigateUpFromSameTask(this);
    }
}