package com.biorecorder.bdfrecorder;

interface SerialListener {
    void onSerialConnect      ();
    void onSerialRead         (byte[] data);
    void onSeriaDisconnect    (Exception e);
}
