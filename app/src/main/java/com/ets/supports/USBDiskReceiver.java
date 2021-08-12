package com.ets.supports;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class USBDiskReceiver extends BroadcastReceiver {
    private static final String TAG = "USBDiskReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String path = intent.getData().getPath();
        Log.e(TAG, "onReceive: action = " + action);
        Log.e(TAG, "onReceive: path = " + path);
        if (!TextUtils.isEmpty(path)) {//这里需要实测下回调返回情况
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)) {
                Log.e(TAG, "onReceive: ---------------usb拨出-------------");
                Log.e(TAG,"path = " + path);
                Log.e(TAG,"isMounted() = " + isMounted());
                //Log.e(TAG,"searchPath()() = " + searchPath());
            }
            if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                Log.e(TAG, "onReceive: --------usb路径-------" + path);
                Log.e(TAG,"path = " + path);
                Log.e(TAG,"isMounted() = " + isMounted());
                Log.e(TAG,"searchPath()() = " + searchPath());
                LogUtil.writeLogs();
                LogUtil.getLog();
                LogcatFileManager.getInstance().start(path);
            }
        }
    }

    private void writeLog(String uPath){

    }




    private static final String MOUNTS_FILE = "/proc/mounts";

    private static String path = "/mnt/usbhost1";

    public static boolean isMounted() {

        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));

            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }

    private static String searchPath() {
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
        if (lineList.size() > 0){
            String editPath = lineList.get(lineList.size() - 1);
            int start = editPath.indexOf("/mnt");
            int end = editPath.indexOf(" vfat");
            String path = editPath.substring(start, end);
            Log.d("OTAManager", "path: " + path);
            return path;
        }
        return "";
    }
}
