package com.biorecorder.bdfrecorder;

import android.util.Log;

public class LogSerialListener implements SerialListener {
    public static final String TAG = "LogSerialListener";

    @Override
    public void onSerialConnect() {
        Log.e(TAG, "Connected!");
    }

    @Override
    public void onSerialRead(byte[] data) {
        Log.e(TAG, "onSerialRead " + data.length + " bytes");
    }

    @Override
    public void onSeriaDisconnect(Exception e) {
        Log.e(TAG, "Disconnected!");
    }
}
