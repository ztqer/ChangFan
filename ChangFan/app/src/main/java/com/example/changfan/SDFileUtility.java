package com.example.changfan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class SDFileUtility {
    public static String oldApkPath;

    //将原apk和补丁包合并为新apk
    public static void PatchApk(final Handler progressMonitor){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                final String path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/changfan/";
                Thread t=new Thread(){
                    @Override
                    public void run() {
                        int result= BsPatchUtility.bspatch(oldApkPath,path+"new.apk",path+"apk.patch");
                        if(result==0){
                            Message message=new Message();
                            Bundle bundle=new Bundle();
                            bundle.putString("install","install");
                            message.setData(bundle);
                            progressMonitor.sendMessage(message);
                        }
                    }
                };
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //安装新apk
    public static void InstallApk(Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            File apkFile = new File(Environment.getExternalStorageDirectory().getCanonicalPath() + "/changfan/new.apk");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = FileProvider.getUriForFile(context, "com.example.changfan.fileprovider", apkFile);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        } catch (IOException e) {
        }
    }

    // 计算文件的 MD5 值
    public static String GetMd5(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        String result = "";
        byte buffer[] = new byte[8192];
        int len;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            byte[] bytes = md5.digest();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
