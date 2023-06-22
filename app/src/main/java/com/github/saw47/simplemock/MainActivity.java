package com.github.saw47.simplemock;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import com.github.saw47.simplemock.databinding.ActivityMainBinding;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private float latitude;
    private float longitude;
    private boolean state;

    private final String LOG_TAG = "SLMACT";
    private final String MAN = "When you first turn it on, select Simplemock as location mock application.\n" +
                               "1. Put coordinates or select Ekibastuz, then press ON.\n" +
                               "2. Force-stop Navigator in user settings and start it again.";

    public static final String PREFERENCES_LAT = "latitude";
    public static final String PREFERENCES_LON = "longitude";
    private SharedPreferences mSettings;

    BroadcastReceiver br;

    public final static String STATE_SERVICE = "state";
    public final static String LAT_SERVICE = "lat";
    public final static String LON_SERVICE = "lon";
    public final static String BROADCAST_ACTION = "com.github.saw47.simplemock.servicebroadcast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate start");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        state = getMockLocationServiceState();

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                state = intent.getBooleanExtra(STATE_SERVICE, false);
                Float tmp_latitude = intent.getFloatExtra(LAT_SERVICE, 0.00F);
                Float tmp_longitude = intent.getFloatExtra(LON_SERVICE, 0.00F);
                setButtonColor(state);

                if (state) {
                    if ((tmp_latitude.compareTo(latitude) != 0) && !binding.latInput.hasFocus()) {
                        latitude = tmp_latitude;
                        binding.latInput.setText(String.format(Locale.US, "%.6f", latitude));
                    }
                    if ((tmp_longitude.compareTo(longitude) != 0) && !binding.lonInput.hasFocus()) {
                        longitude = tmp_longitude;
                        binding.lonInput.setText(String.format(Locale.US,"%.6f", longitude));
                    }
                }

                Log.d(LOG_TAG, "onReceive " + latitude + " " + longitude);
                    }
        };
        registerReceiver(br, new IntentFilter(BROADCAST_ACTION));

        mSettings = getPreferences(Context.MODE_PRIVATE);

        if (!state) {
            if(mSettings.contains(PREFERENCES_LAT)) {
                latitude = mSettings.getFloat(PREFERENCES_LAT, 0.00F);
                Log.d(LOG_TAG, "PREFERENCES_LAT " + latitude);
            }
            if (mSettings.contains(PREFERENCES_LON)) {
                longitude = mSettings.getFloat(PREFERENCES_LON, 0.00F);
                Log.d(LOG_TAG, "PREFERENCES_LON " + longitude);
            }
            binding.latInput.setText(String.format(Locale.US, "%.6f", latitude));
            binding.lonInput.setText(String.format(Locale.US,"%.6f", longitude));
        }


        Log.d(LOG_TAG, "binding...Input.setText " + latitude + " " + longitude);
        setButtonColor(state);

        binding.latInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                float tempLatitude;
                if(!hasFocus) {
                    try {
                        tempLatitude = Float.parseFloat(binding.latInput.getText().toString());
                        if (Float.compare(tempLatitude, latitude) != 0) {
                            latitude = tempLatitude;
                            if (getMockLocationServiceState()) {
                                startMockLocationService();
                            }
                        }
                        Log.i(LOG_TAG, "onFocusChange latInput is double -> " + latitude);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "onFocusChange latInput is not double -> " + e);
                    }
                }
            }
            });

        binding.lonInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                float tempLongitude;
                if(!hasFocus) {
                    try {
                        tempLongitude = Float.parseFloat(binding.lonInput.getText().toString());
                        if (Float.compare(tempLongitude, longitude) != 0) {
                            longitude = tempLongitude;
                            if (getMockLocationServiceState()) {
                                startMockLocationService();
                            }
                        }
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
                startMockLocationService();
            }
        });

        binding.offButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearFocus();
                stopMockLocationService();
            }
        });

        binding.autofillButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                latitude = 51.729692F;
                longitude = 75.326629F;
                binding.latInput.setText(String.format(Locale.US,"%.6f", latitude));
                binding.lonInput.setText(String.format(Locale.US,"%.6f", longitude));
                clearFocus();
                if (getMockLocationServiceState()) {
                    startMockLocationService();
                }
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

        binding.man.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(LOG_TAG, "man call");
                Toast.makeText(getApplicationContext(), MAN, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putFloat(PREFERENCES_LAT, latitude);
        editor.putFloat(PREFERENCES_LON, longitude);
        editor.apply();
        Log.d(LOG_TAG, "onPause; latitude " + latitude + "; longitude " + longitude);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    private void startMockLocationService() {
        Intent intent = new Intent(MainActivity.this, SimpleMockService.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        getApplicationContext().startForegroundService(intent);
    }

    private void stopMockLocationService() {
        stopService(new Intent(MainActivity.this, SimpleMockService.class));
    }

    private void clearFocus() {
        binding.latInput.clearFocus();
        binding.lonInput.clearFocus();
    }

    private void setButtonColor(boolean state) {
        binding.autofillButton.setBackgroundColor(getResources().getColor(R.color.unselected));
        if (state) {
            binding.onButton.setBackgroundColor(getResources().getColor(R.color.selected));
            binding.offButton.setBackgroundColor(getResources().getColor(R.color.unselected));
            binding.onButton.setTextColor(getResources().getColor(R.color.text_color_selected));
            binding.offButton.setTextColor(getResources().getColor(R.color.text_color_base));
        } else {
            binding.onButton.setBackgroundColor(getResources().getColor(R.color.unselected));
            binding.offButton.setBackgroundColor(getResources().getColor(R.color.selected));
            binding.onButton.setTextColor(getResources().getColor(R.color.text_color_base));
            binding.offButton.setTextColor(getResources().getColor(R.color.text_color_selected));
        };
    }

    private boolean getMockLocationServiceState() {
        boolean state = false;
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> rsiList = (ArrayList<ActivityManager.RunningServiceInfo>) am.getRunningServices (Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo rsi: rsiList) {
            state = rsi.service.getPackageName().equals("com.github.saw47.simplemock");
        }
        return state;
    }
}