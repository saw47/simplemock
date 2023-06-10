package com.github.saw47.simplemock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.saw47.simplemock.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private double latitude;
    private double longitude;
    private final String LOG_TAG = "SLM";


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
            Log.i(LOG_TAG, "latitude -> " + latitude + "longitude -> " + longitude);
            binding.latInput.setText(String.format("%.5f", latitude));
            binding.lonInput.setText(String.format("%.5f", longitude));
        }

        binding.latInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        latitude = Double.parseDouble(binding.latInput.getText().toString());
                        Log.i(LOG_TAG, "Value latInput is double -> " + latitude);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "Value latInput is not double -> " + e);
                    }
                }
            }
            });

        binding.lonInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        longitude = Double.parseDouble(binding.lonInput.getText().toString());
                        Log.i(LOG_TAG, "Value lonInput is double -> " + longitude);
                    } catch (NumberFormatException | NullPointerException e) {
                        Log.i(LOG_TAG, "Value lonInput is not double -> " + e);
                    }
                }
            }
        });

        binding.onButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "clicked ON", Toast.LENGTH_LONG).show();
            }
        });

        binding.offButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "clicked OFF", Toast.LENGTH_LONG).show();
            }
        });

        binding.mainContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        binding.collapseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "onSaveInstanceState -> "+latitude+" "+longitude);
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        latitude = savedInstanceState.getDouble("latitude");
        longitude = savedInstanceState.getDouble("longitude");
        Log.i(LOG_TAG, "onSaveInstanceState -> "+latitude+" "+longitude);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume ");
    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }
}