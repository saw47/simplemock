package com.github.saw47.simplemock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import com.github.saw47.simplemock.databinding.ActivityMainBinding;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = "SLMACT";

    private ActivityMainBinding binding;

    private float latitude;
    private float longitude;
    private int speed;

    private boolean state;
    private boolean route_switch;
    private String picked_uri = "";

    public static final String PREFERENCES_LAT = "latitude";
    public static final String PREFERENCES_LON = "longitude";
    public static final String PREFERENCES_STATE = "switch";
    public static final String PREFERENCES_URI = "uri";
    public static final String PREFERENCES_SPEED = "speed";
    private SharedPreferences mSettings;

    BroadcastReceiver br;

    public final static String STATE_SERVICE = "state";
    public final static String LAT_SERVICE = "lat";
    public final static String LON_SERVICE = "lon";
    public final static String BROADCAST_ACTION = "com.github.saw47.simplemock.servicebroadcast";

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            assert uri != null;
                            String filePath = uri.getPath();
                            picked_uri = (filePath != null) ? filePath : "";
                            setPickerAttr(picked_uri);
                            Log.d(LOG_TAG, "onActivityResult uri " + uri.toString());
                            Log.d(LOG_TAG, "onActivityResult path " + filePath);
                        }
                    }
                }
            });

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate start");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mSettings = getPreferences(Context.MODE_PRIVATE);

        if (mSettings.contains(PREFERENCES_STATE)) {
            route_switch = mSettings.getBoolean(PREFERENCES_STATE, false);
            Log.d(LOG_TAG, "PREFERENCES_STATE route switch " + route_switch);
        }

        if (mSettings.contains(PREFERENCES_URI)) {
            picked_uri = mSettings.getString(PREFERENCES_URI,"");
            setPickerAttr(picked_uri);
        }

        if (mSettings.contains(PREFERENCES_SPEED)) {
            speed = mSettings.getInt(PREFERENCES_SPEED,0);
            binding.speedInput.setText(String.valueOf(speed));
        }

        setPickerAttr(picked_uri);
        setOnRouteSwitchAttr(route_switch);
        binding.switchMode.setChecked(route_switch);

        setContentView(binding.getRoot());
        state = getMockLocationServiceState();
        set_state_semaphore(state);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                state = intent.getBooleanExtra(STATE_SERVICE, false);
                Float tmp_latitude = intent.getFloatExtra(LAT_SERVICE, 0.00F);
                Float tmp_longitude = intent.getFloatExtra(LON_SERVICE, 0.00F);
                set_state_semaphore(state);
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

        binding.speedInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                int tempSpeed;
                if(!hasFocus) {
                    try {
                        tempSpeed = Integer.parseInt(binding.speedInput.getText().toString());
                        binding.speedInput.setText(String.valueOf(tempSpeed));
                        if (tempSpeed != speed) {
                            speed = tempSpeed;
                            if (getMockLocationServiceState()) {
                                startMockLocationService();
                            }
                        }
                        Log.i(LOG_TAG, "onFocusChange speed is ok -> " + speed);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "onFocusChange speed is not int -> " + e);
                    }
                }
            }
        });

        binding.onButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMockLocationServiceState(true);
            }
        });

        binding.onRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMockLocationServiceState(true);
            }
        });

        binding.offButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMockLocationServiceState(false);
            }
        });

        binding.offRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setMockLocationServiceState(false);
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

        binding.autofillButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopMockLocationService();
                latitude = longitude = 0F;
                binding.latInput.setText(String.format(Locale.US,"%.6f", latitude));
                binding.lonInput.setText(String.format(Locale.US,"%.6f", longitude));
                return true;
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

        binding.devInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(LOG_TAG, "devInfo call");
                startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS));
            }
        });

        binding.devSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(LOG_TAG, "settings call");
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            }
        });

        binding.man.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(LOG_TAG, "man call");
                startActivity(new Intent(v.getContext(), InfoActivity.class));
            }
        });

        binding.switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            stopMockLocationService();
            route_switch = isChecked;
            setOnRouteSwitchAttr(route_switch);
        });

        binding.pickRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                mStartForResult.launch(intent);
            }
        });

        binding.pickRouteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopMockLocationService();
                picked_uri = "";
                speed = 0;
                binding.speedInput.setText(String.valueOf(speed));
                setPickerAttr(picked_uri);
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putFloat(PREFERENCES_LAT, latitude);
        editor.putFloat(PREFERENCES_LON, longitude);
        editor.putBoolean(PREFERENCES_STATE, route_switch);
        editor.putString(PREFERENCES_URI, picked_uri);
        editor.putInt(PREFERENCES_SPEED, speed);
        editor.apply();
        Log.d(LOG_TAG, "onPause; latitude " + latitude + "; longitude " +
                longitude + "; route_switch " + route_switch);
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
        binding.speedInput.clearFocus();
    }

    private void set_state_semaphore(Boolean service_state) {
        if (service_state) {
            binding.semaphore.setColorFilter(getResources().getColor(R.color.on_service));
            Log.i(LOG_TAG, "GREEN call");
        } else {
            binding.semaphore.setColorFilter(getResources().getColor(R.color.off_service));
            Log.i(LOG_TAG, "RED call");
        }
    }

    private void setOnRouteSwitchAttr(boolean route_switch) {
        if (route_switch) {
            binding.staticItems.setVisibility(View.GONE);
            binding.routeItems.setVisibility(View.VISIBLE);
        } else {
            binding.routeItems.setVisibility(View.GONE);
            binding.staticItems.setVisibility(View.VISIBLE);
        }
    }

    private void setPickerAttr(String picked) {
        Log.i(LOG_TAG, "setIsPickedAttr call " + picked);
        String [] text = picked.split(":");
        if (text.length > 1) {
            binding.selected.setText(text[text.length - 1]);
        } else
            binding.selected.setText(picked);
        if (!picked.equals("")) {
            binding.selectedCv.setVisibility(View.VISIBLE);
        } else {
            binding.selectedCv.setVisibility(View.GONE);
        }
    }

    private void setMockLocationServiceState(boolean is_started) {
        if (is_started) {
            clearFocus();
            startMockLocationService();
        } else {
            clearFocus();
            stopMockLocationService();
        }
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