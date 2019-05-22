package com.biorecorder.bdfrecorder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

class SerialSocket implements Runnable {

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String TAG = "SerialSocket";
    public static final String DEVICE_NAME = "BIOREC";
    private volatile boolean isAutoReconnect = true;
    private SerialListener listener;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private boolean connected;

    void  connect(SerialListener listener, boolean isAutoReconnect) throws IOException {
        if(connected || socket != null) {
            throw new IOException("already connected");
        }
        this.listener = listener;
        this.isAutoReconnect = isAutoReconnect;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()){
            if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
                if(DEVICE_NAME.equals(device.getName())) {
                    this.device = device;
                }
            }
        }
        if(device == null){
            throw new IOException("No BIOREC device was found");
        }
        Executors.newSingleThreadExecutor().submit(this);
    }

    void disconnect() {
        listener = null;
        isAutoReconnect = false;
        if(socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }

    void write(byte[] data) throws IOException {
        if (!connected) {
            throw new IOException("not connected");
        }
        socket.getOutputStream().write(data);
    }

    @Override
    public void run() {
        while(isAutoReconnect) {
            try {
                socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
                socket.connect();
                if (listener != null) {
                    listener.onSerialConnect();
                }
                connected = true;
                Log.e(TAG, "Connected!");
                byte[] buffer = new byte[1024];
                int len;
                while (true) {
                    len = socket.getInputStream().read(buffer);
                    byte[] data = Arrays.copyOf(buffer, len);
                    if (listener != null)
                        listener.onSerialRead(data);
                }
            } catch (Exception e) {
                connected = false;
                Log.e(TAG, "Disconnected", e);
                if (listener != null) {
                    listener.onSeriaDisconnect(e);
                }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            }
        }
    }
}
