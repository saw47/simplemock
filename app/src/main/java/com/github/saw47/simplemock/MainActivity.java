package com.github.saw47.simplemock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import com.github.saw47.simplemock.databinding.ActivityMainBinding;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private float latitude;
    private float longitude;
    private boolean state;

    private final String LOG_TAG = "SLMACT";

    public static final String PREFERENCES_LAT = "latitude";
    public static final String PREFERENCES_LON = "longitude";
    public static final String PREFERENCES_STATE = "state";
    private SharedPreferences mSettings;

    BroadcastReceiver br;
    public final static String STATE_SERVICE = "state";
    public final static String BROADCAST_ACTION = "com.github.saw47.simplemock.servicebroadcast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                state = intent.getBooleanExtra(STATE_SERVICE, false);
                setButtonColor(state);
                Log.d(LOG_TAG, "BroadcastReceiver onReceive , state - " + state);
                    }
        };

        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, filter);

        mSettings = getPreferences(Context.MODE_PRIVATE);

        if (mSettings.contains(PREFERENCES_STATE)) {
            state = mSettings.getBoolean(PREFERENCES_STATE, false);
        } else {
            state = false;
        }

        if(mSettings.contains(PREFERENCES_LAT)) {
            latitude = mSettings.getFloat(PREFERENCES_LAT, 0.00F);
            binding.latInput.setText(String.format(Locale.US, "%.5f", latitude));
            Log.d(LOG_TAG, "latitude " + latitude);
        }

        if (mSettings.contains(PREFERENCES_LON)) {
            longitude = mSettings.getFloat(PREFERENCES_LON, 0.00F);
            binding.lonInput.setText(String.format(Locale.US,"%.5f", longitude));
            Log.d(LOG_TAG, "longitude " + longitude);
        }

        setButtonColor(state);

        binding.latInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        latitude = Float.parseFloat(binding.latInput.getText().toString());
                        Log.i(LOG_TAG, "onFocusChange latInput is double -> " + latitude);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "onFocusChange latInput is not double -> " + e);
                    }
                }
            }
            });

        binding.lonInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        longitude = Float.parseFloat(binding.lonInput.getText().toString());
                        Log.i(LOG_TAG, "onFocusChange lonInput is double -> " + longitude);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "onFocusChange lonInput is not double -> " + e);
                    }
                }
            }
        });

        binding.onButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearFocus();
                Intent intent = new Intent(MainActivity.this, SimpleMockService.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startService(intent);
            }
        });

        binding.offButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearFocus();
                stopService(new Intent(MainActivity.this, SimpleMockService.class));
            }
        });

        binding.mainContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        binding.collapseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearFocus();
                onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putFloat(PREFERENCES_LAT, latitude);
        editor.putFloat(PREFERENCES_LON, longitude);
        editor.putBoolean(PREFERENCES_STATE, state);
        editor.apply();
        Log.d(LOG_TAG, "onPause; latitude " + latitude + "; longitude " + longitude);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    private void clearFocus() {
        binding.latInput.clearFocus();
        binding.lonInput.clearFocus();
    }
    private void setButtonColor(boolean state) {
        if (state) {
            binding.onButton.setBackgroundColor(getResources().getColor(R.color.selected_orange));
            binding.offButton.setBackgroundColor(getResources().getColor(R.color.unselected_orange));
            binding.onButton.setTextColor(getResources().getColor(R.color.black));
            binding.offButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            binding.onButton.setBackgroundColor(getResources().getColor(R.color.unselected_orange));
            binding.offButton.setBackgroundColor(getResources().getColor(R.color.selected_orange));
            binding.onButton.setTextColor(getResources().getColor(R.color.white));
            binding.offButton.setTextColor(getResources().getColor(R.color.black));
        }
    }

}