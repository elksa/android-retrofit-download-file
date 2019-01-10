package com.elksa.retrofitdownloadfile;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elksa.retrofitdownloadfile.model.Download;
import com.elksa.retrofitdownloadfile.services.DownloadService;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private ProgressBar mProgressBar;
    private TextView mProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.progress);
        mProgressText = findViewById(R.id.progress_text);

        Button btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission()){
                    startDownload();
                }
                else {
                    requestPermission();
                }
            }
        });

        registerReceiver();
    }

    private void startDownload(){
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }

    private void registerReceiver(){

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction() != null && intent.getAction().equals(MESSAGE_PROGRESS)){

                Download download = intent.getParcelableExtra("download");
                mProgressBar.setProgress(download.getProgress());
                if(download.getProgress() == 100){
                    mProgressText.setText("File Download Complete");
                }
                else {
                    mProgressText.setText(String.format("Downloaded (%d/%d) MB",
                            download.getCurrentFileSize(), download.getTotalFileSize()));

                }
            }
        }
    };

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            "Permission Denied, Please allow to proceed !", Snackbar.LENGTH_LONG).show();

                }
                break;
        }
    }

}
