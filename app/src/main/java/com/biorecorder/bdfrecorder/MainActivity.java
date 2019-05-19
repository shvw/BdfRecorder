package com.biorecorder.bdfrecorder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
        setContentView(R.layout.activity_main);
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
        textView.setText(bdfRecorderService.getTraliVali());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity","onResume");
        Intent startIntent= new Intent(this, BdfRecorderService.class);
        //startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        bindService(startIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("MainActivity","onPause");
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.e("MainActivity","onServiceConnected");
        BdfRecorderService.ServiceBinder b = (BdfRecorderService.ServiceBinder) binder;
        bdfRecorderService = b.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e("MainActivity","onServiceDisconnected");
        bdfRecorderService = null;
    }
}
