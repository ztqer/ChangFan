package com.example.changfan.Handler;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class BroadcastHandler extends AbstractHandler{
    private Context context;
    private String tag;
    //构造接收1.Context来使用广播 2.tag用于BroadcastReceiver判别
    public BroadcastHandler(Context context,String tag){
        this.context=context;
        this.tag=tag;
    }

    @Override
    protected String GetName() {
        return "Broadcast";
    }

    @Override
    protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        while(true){
            int len=is.read(buffer);
            if(len==0){
                continue;
            }
            String s=new String(buffer,0,len);
            MatchAndBroadcast(s);
        }
    }

    //根据tag进行正则匹配，通过后发出notification
    private void MatchAndBroadcast(String s){
        String pattern=tag+".*";
        if(Pattern.matches(pattern,s)){
            String s1=s.substring(tag.length()+1);
            //把字符串封装进Intent并发布本地广播
            Intent intent=new Intent("sendNotification");
            intent.putExtra("tag",tag);
            intent.putExtra("message",s1);
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(context);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}