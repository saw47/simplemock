package com.github.saw47.simplemock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SimpleMockService extends Service {
    private final String LOG_TAG = "SLMSRV";

    public SimpleMockService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Toast.makeText(this, "clicked ON", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        callBack(true);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        callBack(false);
        Toast.makeText(this, "clicked OFF", Toast.LENGTH_LONG).show();
    }

    public void callBack(boolean result) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        Log.d(LOG_TAG, "callBack");
        intent.putExtra(MainActivity.STATE_SERVICE, result);
        sendBroadcast(intent);
    }
}
