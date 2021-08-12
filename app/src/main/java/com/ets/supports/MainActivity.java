package com.ets.supports;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ets.ota.OTADialog;
import com.ets.ota.OTAManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    OTAManager otaManager;
    OTADialog otaDialog;
    USBDiskReceiver mUSBDiskReceiver;

    TextView helloTV ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        otaManager = OTAManager.getOtaManagerInstance(getApplicationContext());
        otaDialog = new OTADialog(this);


        helloTV = findViewById(R.id.hello);
        helloTV.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this).setMessage("更新中").setTitle("系统升级").setPositiveButton(
                    "确定", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            showOTADialog();

                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
            }
        });

    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUSBDiskReceiver);
    }

    private void showOTADialog (){
        otaDialog.show();
    }

    private void startOTA(){
        otaManager.download("https://codeload.github.com/xuexiangjys/XUI/zip/refs/heads/master",null,null);

        otaManager.verify(new File(OTAManager.OTA_FILE_DOWNLOAD_PATH),
            new RecoverySystem.ProgressListener() {
                @Override public void onProgress(int progress) {
                    otaDialog.setProgress(progress);
                }
            });
        otaManager.install(new File(OTAManager.OTA_FILE_DOWNLOAD_PATH));
    }


    private static final int MSG_UPDATE_DOWNLOAD_PROGRESS = 1;
    private static final int MSG_UPDATE_VERIFY_PROGRESS = 2;

    private static class UiHandler extends Handler{
        WeakReference<MainActivity> mActivity = null;
        public UiHandler (MainActivity mainActivity){
            this.mActivity = new WeakReference<MainActivity>(mainActivity);
        }

        @Override public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_DOWNLOAD_PROGRESS:
                    //
                    break;
                case MSG_UPDATE_VERIFY_PROGRESS:
                    //
                    break;
                default:
                    break;
            }
        }
    }
}