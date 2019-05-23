package com.biorecorder.bdfrecorder;

public interface SerialListener {
    void onSerialConnect      ();
    void onSerialRead         (byte[] data);
    void onSeriaDisconnect    (Exception e);
}
