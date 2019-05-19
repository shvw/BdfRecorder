package com.biorecorder.bdfrecorder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
        bdfRecorderService.stopForeground(true);
        bdfRecorderService.stopSelf();
        TextView textView = findViewById(R.id.textView);
        textView.setText("stop" );
    }

    public void onExitButtonClick(View view){
        TextView textView = findViewById(R.id.textView);
        textView.setText("exit");
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
