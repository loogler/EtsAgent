package com.ets.ota;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.RecoverySystem;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;

public class OTADialog extends Dialog {
    OTAManager otaManager;
    Context context;
    DownloadReceiver receiver;
    Handler handler = new Handler();

    private boolean isRegisterReceiver;
    private long downloadId = 0;
    /**支持的类型为 1:在线升级  2:本地升级*/
    private int otaType = 0;
    private File filePath;

    TextView titleView;
    TextView messageView;
    ProgressBar progressView;
    TextView percentView;

    public OTADialog(@NonNull Context context) {
        super(context);
    }
    public OTADialog(@NonNull Context context,OTAManager otaManager,int otaType) {
        super(context);
        this.context = context;
        this.otaManager = otaManager;
        this.otaType = otaType;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otadialog);
        titleView = findViewById(R.id.title);
        messageView = findViewById(R.id.message);
        progressView = findViewById(R.id.progress);
        percentView =  findViewById(R.id.percent);

        new Thread(new Runnable() {
            @Override public void run() {
                if (otaType == 1){
                    filePath = new File(OTAManager.OTA_FILE_DOWNLOAD_PATH);
                    startOTA();
                }else if (otaType ==2){
                    filePath = new File(OTAManager.OTA_FILE_LOCAL_PATH);
                    verifyFile();
                }else {
                    Log.d("","invalid OTA type");
                }
            }
        }).start();

    }

    public void setProgress (int progress){
        progressView.setProgress(progress);
    }

    public void setTitleView(String title){
        titleView.setText(title);
    }

    public void setMessageView(String message){
        messageView.setText(message);
    }

    public void setPercentView (String percent){
        percentView.setText(String.format("%d \\%",percent));
    }
    @Override public void show() {
        //titleView.setText("系统升级");
        //messageView.setText("下载中");
        //progressView.setProgress(0);
        //percentView.setText(String.format("%d \\%","0"));
        super.show();
    }

    @Override public void dismiss() {
        if (receiver != null)
            context.unregisterReceiver(receiver);
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.dismiss();
    }

    /**
     * 执行下载程序
     */
    private void startOTA(){
        if (downloadId != 0){
            otaManager.clearCurrentTask(downloadId);
        }
        downloadId = otaManager.download("https://codeload.github.com/xuexiangjys/XUI/zip/refs/heads/master",null,null);
        handler.post(count);
        setReceiver();
    }

    /**
     * 执行校验程序
     */
    private void verifyFile(){
        otaManager.verify(filePath,
            new RecoverySystem.ProgressListener() {
                @Override public void onProgress(int progress) {
                    setMessageView("正在校验文件");
                    setProgress(progress);
                    setPercentView(String.valueOf(progress));
                    if (progress >= 100){
                        setMessageView("校验成功");
                        install();
                    }
                }
            });
    }

    /**
     * 执行安装程序
     */
    private void install(){
        otaManager.install(filePath);
    }


    /**
     * 注册下载成功的广播监听
     */
    private void setReceiver() {
        if (!isRegisterReceiver) {
            receiver = new DownloadReceiver();
            IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            context.registerReceiver(receiver, intentFilter);
            isRegisterReceiver = true;
        }
    }

    /**
     *
     * 下载成功广播类
     */
    public class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                verifyFile();

            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                //处理 如果还未完成下载，用户点击Notification ，跳转到下载中心
                //Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                //viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //context.startActivity(viewDownloadIntent);
            }
        }
    }

    private Runnable count = new Runnable() {
        @Override public void run() {
            handler.postDelayed(this,5000);
            otaManager.getProgress(downloadId);
        }
    };

}
