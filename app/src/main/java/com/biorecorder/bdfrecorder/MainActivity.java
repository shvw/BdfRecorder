package com.biorecorder.bdfrecorder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.biorecorder.multisignal.edflib.EdfReader;
import com.biorecorder.multisignal.edflib.EdfWriter;
import com.biorecorder.multisignal.recordformat.DataHeader;

import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
     private BdfRecorderService bdfRecorderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestFileWritingPermision();
        setContentView(R.layout.activity_main);
    }

    private void checkAndRequestFileWritingPermision() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("xxx","permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 13);
        }
    }

    public void onStartButtonClick(View view){
        Intent startIntent = new Intent(MainActivity.this, BdfRecorderService.class);
        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(startIntent);
        TextView textView = findViewById(R.id.textView);
        textView.setText("started" );
    }

    public void onStopButtonClick(View view){
        bdfRecorderService.disconnect();
        bdfRecorderService.stopForeground(true);
        bdfRecorderService.stopSelf();
        TextView textView = findViewById(R.id.textView);
        textView.setText("stop" );
    }

    public void onRecButtonClick(View view){
        byte[] data = {(byte)0x20,(byte)0xF0,(byte)0x11,(byte)0xF1,(byte)0x01,(byte)0x0A,(byte)0x02,
                (byte)0xA0,(byte)0x10,(byte)0x60,(byte)0x91,(byte)0x20,(byte)0x00,(byte)0x40,
                (byte)0x02,(byte)0x01,(byte)0xF2,(byte)0x0A,(byte)0x00,(byte)0xF3,(byte)0x01,
                (byte)0xF4,(byte)0x01,(byte)0xF5,(byte)0x00,(byte)0xF6,(byte)0x00,(byte)0xF0,
                (byte)0x10,(byte)0xFE,(byte)0x55,(byte)0x55};
        bdfRecorderService.write(data);

    }

    public void onStopRecClick(View view){
        byte[] data = {(byte)0xff};
        bdfRecorderService.write(data);
        bdfRecorderService.socketListenerClose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent startIntent= new Intent(this, BdfRecorderService.class);
        //startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        bindService(startIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        BdfRecorderService.ServiceBinder b = (BdfRecorderService.ServiceBinder) binder;
        bdfRecorderService = b.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bdfRecorderService = null;
    }
}
