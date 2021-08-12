package com.ets.ota;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class OTAManager {

    private Context mContext;
    private static OTAManager otaManager;

    public static final String OTA_FILE_DOWNLOAD_PATH = "/data/";
    public static final String OTA_FILE_LOCAL_PATH = "/data/";

    private DownloadManager downloadManager;
    private PowerManager.WakeLock wl;

    @SuppressLint("InvalidWakeLockTag") private OTAManager (Context mContext){
        this.mContext = mContext;
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
    }

    public static synchronized OTAManager getOtaManagerInstance(Context mContext){
        if (otaManager == null)
            otaManager = new OTAManager(mContext);

        return otaManager;
    }

    public static OTAManager getOtaManagerInstance(){
        return otaManager;
    }

    /**
     * 比较实用的升级版下载功能
     *
     * @param url   下载地址
     * @param title 文件名字
     * @param desc  文件路径
     */
    public long download(String url, String title, String desc) {
        long ID;

        //以下两行代码可以让下载的apk文件被直接安装而不用使用Fileprovider,系统7.0或者以上才启动。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder localBuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(localBuilder.build());
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        //7.0以上的系统适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.setRequiresDeviceIdle(false);
            request.setRequiresCharging(false);
        }

        //大于11版本手机允许扫描
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //表示允许MediaScanner扫描到这个文件，默认不允许。
            request.allowScanningByMediaScanner();
        }

        //request.addRequestHeader(String header,String value);//添加网络下载请求的http头信息
        //request.allowScanningByMediaScanner();//用于设置是否允许本MediaScanner扫描。
        //request.setAllowedNetworkTypes(int flags);//设置用于下载时的网络类型，默认任何网络都可以下载，提供的网络常量有：NETWORK_BLUETOOTH、NETWORK_MOBILE、NETWORK_WIFI。
        //request.setAllowedOverRoaming(Boolean allowed);//用于设置漫游状态下是否可以下载
        request.setNotificationVisibility(View.GONE);//用于设置下载时时候在状态栏显示通知信息
        //request.setTitle(CharSequence);//设置Notification的title信息
        //request.setDescription(CharSequence);//设置Notification的message信息
        //setDestinationInExternalFilesDir、setDestinationInExternalPublicDir、setDestinationUri等方法用于设置下载文件的存放路径，注意如果将下载文件存放在默认路径，那么在空间不足的情况下系统会将文件删除，所以使用上述方法设置文件存放目录是十分必要的。

        ID = downloadManager.enqueue(request);
        return ID;
    }

    /**
     * 通过下载ID获取当前下载内容的进度条信息
     * @param id
     * @return
     */
    public int getProgress(long id){
        int progress = 0;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor= downloadManager.query(query);
        String size="0";
        String sizeTotal="0";
        if(cursor.moveToNext()){
            size= cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        }
        cursor.close();
        //progressBar.setMax(Integer.valueOf(sizeTotal));
        //progressBar.setProgress(Integer.valueOf(size));
        progress = Integer.valueOf(size)/ Integer.valueOf(sizeTotal);
        Log.d("getProgress","progress = " + progress);
        //TODO debug this value

        return progress;
    }

    /**
     * 下载前先移除前一个任务，防止重复下载
     *
     * @param downloadId
     */
    public void clearCurrentTask(long downloadId) {
        if (downloadManager == null)
            return;
        try {
            downloadManager.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 将镜像文件检查是否可用
     */
    public void verify (File recoveryFile, RecoverySystem.ProgressListener progressListener){
        try {
            wl.acquire();
            RecoverySystem.verifyPackage(recoveryFile, progressListener,null);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            wl.release();
        }
    }

    /**
     * 检查可用的镜像直接升级系统
     * @param recoveryFile
     */
    public void install(File recoveryFile){
        try {
            RecoverySystem.installPackage(mContext,recoveryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /****************************** tools **********************************************/

    /**
     * 获取U盘挂载路径，用于升级系统时，拿到U盘路径中的文件，再根据路径升级指定文件.
     * 注意U盘文件格式，最好不要用NTFS
     */
    private String searchPath() {
        String filePath = "/proc/mounts";
        File file = new File(filePath);
        List<String> lineList = new ArrayList<>();
        InputStream inputStream =null;
        try {
            inputStream = new FileInputStream(file);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("vfat")) {
                        lineList.add(line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String editPath = lineList.get(lineList.size() - 1);
        int start = editPath.indexOf("/mnt");
        int end = editPath.indexOf(" vfat");
        String path = editPath.substring(start, end);
        Log.d("OTAManager", "path: " + path);

        return path;
    }
}
