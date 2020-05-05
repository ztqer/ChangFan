package com.example.changfan.Handler;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class UpdateHandler extends AbstractHandler {
    private String versionName;
    private Handler processMonitor;
    //构造接收当前版本号和主线程通信的Handler
    public UpdateHandler(String versionName, Handler processMonitor){
        this.versionName=versionName;
        this.processMonitor=processMonitor;
    }
    @Override
    protected String GetName() {
        return "Update";
    }

    @Override
    protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        os.write(versionName.getBytes());
        os.flush();
        int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        SendMessage("isNewest",s1);
        if(s1.equals("当前为最新版本")){
            return;
        }
        os.write(new byte[1]);
        os.flush();
        //读取文件长度
        int len2=is.read(buffer);
        String s2=new String(buffer,0,len2);
        SendMessage("length",s2);
        os.write(new byte[1]);
        os.flush();
        //向指定目录写入补丁包
        int length=0;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            SendMessage("isNewest","请设置存储权限并重启app");
            throw new SocketException();
        }
        File directory=new File(Environment.getExternalStorageDirectory().getCanonicalPath()+"/changfan");
        if(!directory.exists()){
            directory.mkdir();
        }
        File file=new File(Environment.getExternalStorageDirectory().getCanonicalPath()+"/changfan/apk.patch");
        FileOutputStream fileOutputStream=new FileOutputStream(file);
        int len=-1;
        while((len=is.read(buffer))!=-1){
            fileOutputStream.write(buffer,0,len);
            fileOutputStream.flush();
            length+=len;
            SendMessage("len",String.valueOf(length));
        }
        fileOutputStream.close();
        SendMessage("over","over");
    }

    //向主线程handler传递信息
    private void SendMessage(String tag,String s){
        Message message=new Message();
        Bundle bundle=new Bundle();
        bundle.putString(tag,s);
        message.setData(bundle);
        processMonitor.sendMessage(message);
    }
}
