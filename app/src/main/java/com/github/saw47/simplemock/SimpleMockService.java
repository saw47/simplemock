package com.github.saw47.simplemock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class SimpleMockService extends Service {
    private final String LOG_TAG = "SLMSRV";

    private final String CHANNEL_ID = "channelID";
    private final String CHANNEL_NAME = "channelName";
    private final int NOTIFICATION_ID = 101;

    private final Float DEFAULT_ALTITUDE = 0.00F;
    private final Float DEFAULT_BEARING = 0.00F;
    private final Float DEFAULT_SPEED = 00.00F;

    private float latitude;
    private float longitude;

    private Location mockLocation;
    private LocationManager locationManager;
    private final String mockLocationProvider = LocationManager.NETWORK_PROVIDER;
    private final Handler mHandler = new Handler();
    private Notification notification;

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
        try{
            locationManager.addTestProvider(mockLocationProvider, false, false,
                    false, false, false, false,
                    false, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
            locationManager.setTestProviderEnabled(mockLocationProvider, true);
            Log.d(LOG_TAG, "onCreate end");
        } catch (SecurityException e) {
            Log.d(LOG_TAG, "onCreate " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand start");
        latitude = intent.getFloatExtra("latitude", 0.00F);
        longitude = intent.getFloatExtra("longitude", 0.00F);
        Log.d(LOG_TAG, "latitude " + latitude + ";longitude" + longitude);
        mHandler.post(locationSenderRunnable);
        notification = new Notification.Builder(this,createNotificationChannel(CHANNEL_ID, CHANNEL_NAME))
                .setContentTitle("SimpleMock")
                .setContentText("Mock location in progress, latitude-" + latitude + "longitude-" + longitude)
                .setAutoCancel(false)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        Log.d(LOG_TAG, "onStartCommand end");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        mHandler.removeCallbacks(locationSenderRunnable);
        callBack(false);
    }

    public void callBack(boolean result) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.STATE_SERVICE, result);
        intent.putExtra(MainActivity.LAT_SERVICE, latitude);
        intent.putExtra(MainActivity.LON_SERVICE, longitude);
        sendBroadcast(intent);
    }

    private final Runnable locationSenderRunnable = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.S)
        public void run() {
            mockLocation = new Location(mockLocationProvider);
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
                Log.d(LOG_TAG, "setTestProviderLocation start success, latitude:"+latitude+
                        ", longitude:"+longitude);
                callBack(true);
            } catch (Exception e) {
                Log.d(LOG_TAG, "setTestProviderLocation exception -> " + e);
                Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_SHORT).show();
                callBack(false);
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    private String createNotificationChannel(String channelId ,String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);
        return channelId;
    }
}
