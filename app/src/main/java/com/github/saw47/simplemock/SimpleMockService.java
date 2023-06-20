package com.github.saw47.simplemock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class SimpleMockService extends Service {
    private final String LOG_TAG = "SLMSRV";

    private final Float DEFAULT_ALTITUDE = 0.00F;
    private final Float DEFAULT_BEARING = 0.00F;
    private final Float DEFAULT_SPEED = 0.00F;

    private LocationManager locationManager;
    private final String mockLocationProvider = LocationManager.NETWORK_PROVIDER;

    public SimpleMockService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate start");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider(mockLocationProvider, false, false,
                false, false, false, false,
                false, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(mockLocationProvider, true);
        Log.d(LOG_TAG, "onCreate end");
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand start");
        Location mockLocation = new Location(mockLocationProvider);
        float latitude = intent.getFloatExtra("latitude", 51.729692F);
        float longitude = intent.getFloatExtra("longitude", 75.326629F);
        Log.d(LOG_TAG, "latitude " + latitude + ";longitude" + longitude);
        mockLocation.setLatitude(latitude);
        mockLocation.setLongitude(longitude);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAltitude(DEFAULT_ALTITUDE);
        mockLocation.setAccuracy(ProviderProperties.ACCURACY_FINE);
        mockLocation.setBearing(DEFAULT_BEARING);
        mockLocation.setSpeed(DEFAULT_SPEED);
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


        try{
            locationManager.setTestProviderLocation( mockLocationProvider, mockLocation);
            callBack(true);
            Log.d(LOG_TAG, "onStartCommand end");
        } catch (Exception e) {
            Log.d(LOG_TAG, "onStartCommand " + e);
            callBack(true);
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        callBack(false);
    }

    public void callBack(boolean result) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        Log.d(LOG_TAG, "callBack " + result);
        intent.putExtra(MainActivity.STATE_SERVICE, result);
        sendBroadcast(intent);
    }
}
