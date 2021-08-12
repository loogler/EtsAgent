package com.ets.supports;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class LogUtil {



    public static void getLog (){
        try {
            ArrayList commandLine = new ArrayList();
            commandLine.add( "logcat");
            commandLine.add( "-d");//使用该参数可以让logcat获取日志完毕后终止进程
            commandLine.add( "-v");
            commandLine.add( "time");
            commandLine.add( "-f");//如果使用commandLine.add(">");是不会写入文件，必须使用-f的方式
            commandLine.add( "/sdcard/log/logcat.txt");
            String t = String.valueOf(commandLine.toArray( new String[commandLine.size()]));
            Process process = Runtime.getRuntime().exec(t);
            //BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()), 1024);
            //String line = bufferedReader.readLine();
            //while ( line != null) {
            //    log.append(line);
            //    log.append("\n");
            //}
        } catch ( IOException e) {
        }
    }

    public static void writeLogs(){
    String TAG = "LogUtil";
    String shell1 = "adb root";
    String shell2 = "logcat";
    try {
        //Process process1 = Runtime.getRuntime().exec(shell1);
        Process process2 = Runtime.getRuntime().exec(shell2);
    //    InputStream inputStream = process.getInputStream();
    //
    //
    //    boolean sdCardExist = Environment.getExternalStorageState().equals(
    //        android.os.Environment.MEDIA_MOUNTED);
    //    File dir = null;
    //    if (sdCardExist)
    //    {
    //        dir = new File(searchPath()
    //            + File.separator + "logcatwyx.txt");
    //        if (!dir.exists())
    //        {
    //            dir.createNewFile();
    //        }
    //
    //    }
    //    byte[] buffer = new byte[1024];
    //    int bytesLeft = 5 * 1024 * 1024; // Or whatever
    //    try
    //    {
    //        FileOutputStream fos = new FileOutputStream(dir);
    //        try
    //        {
    //            while (bytesLeft > 0)
    //            {
    //                int read = inputStream.read(buffer, 0, Math.min(bytesLeft,
    //                    buffer.length));
    //                if (read == -1)
    //                {
    //                    throw new EOFException("Unexpected end of data");
    //                }
    //                fos.write(buffer, 0, read);
    //                bytesLeft -= read;
    //            }
    //        } finally
    //        {
    //            fos.close(); // Or use Guava's
    //            // Closeables.closeQuietly,
    //            // or try-with-resources in Java 7
    //        }
    //    } finally
    //    {
    //        inputStream.close();
    //    }
    //    //                    String logcat = convertStreamToString(inputStream);
    //    //                    outputFile2SdTest(logcat, "logwyx.txt");
    //    Log.v(TAG, "LOGCAT = ok" );
    } catch (IOException e)
    {
        e.printStackTrace();
    }

    }

    private static BufferedWriter writer = null;
    //读取log信息保存到本地文件
    @SuppressWarnings("resource")
    private static void WriteStringToFile(String srcFilePath, String data,boolean closeWriter) {
        File srcFile = new File(srcFilePath);
        //创建txt文本准备写入log
        if (!srcFile.exists()) {
            try {
                srcFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入log
        try {

            if (srcFile.exists()) {
                if (writer == null) {
                    //FileOutputStream outputStream = new FileOutputStream(srcFile, false);
                    //OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
                    //BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
                    writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(srcFile, false), "UTF-8"));
                } else {
                    writer.append(data + "\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null && closeWriter) {
                    //关流
                    writer.close();
                    writer = null;
                    //复制log文件到U盘
                    String desPaths = searchPath();
                    if (desPaths != null) {
                        String desPath = desPaths+ "/log/" + "/";
                        //创建目录
                        File desDirFile = new File(desPath);
                        if (!desDirFile.exists()) {
                            desDirFile.mkdir();
                        }

                        //复制到U盘
                        Runtime.getRuntime().exec("cp " + srcFile + " " + desPath);
                        Runtime.getRuntime().exec("ls -l " + desPath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        String editPath = lineList.get(lineList.size() - 1);
        int start = editPath.indexOf("/mnt");
        int end = editPath.indexOf(" vfat");
        String path = editPath.substring(start, end);
        Log.d("OTAManager", "path: " + path);

        return path;
    }
}
