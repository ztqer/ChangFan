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
    public volatile boolean isover=false;
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
            if(isover){
                break;
            }
            int len=is.read(buffer);
            if(len>1){
                String s=new String(buffer,0,len);
                MatchAndBroadcast(s);
            }
            //连接心跳
            os.write(new byte[1]);
            os.flush();
        }
    }

    private void MatchAndBroadcast(String s){
        //根据tag进行正则匹配，通过后发出notification
        Intent intent=new Intent();
        String pattern=tag+".*";
        if(Pattern.matches(pattern,s)){
            String s1=s.substring(tag.length()+1);
            //把字符串封装进Intent并发布本地广播
            intent.setAction("sendNotification");
            intent.putExtra("tag",tag);
            intent.putExtra("message",s1);
        }
        //重复登陆，回到LoginActivity
        if(s.length()>8&&s.substring(0,7).equals("warning")){
            intent.setAction("reLogin");
            intent.putExtra("username",s.substring(8));
        }
        //防止管理员用户库存列表更新两次
        if(tag.equals("root")){
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(context);
            localBroadcastManager.sendBroadcast(intent);
        }
        else {
            //数据更新
            if(s.length()>7&&s.substring(0,6).equals("record")){
                intent.setAction("update");
                intent.putExtra("data",s.substring(7));
            }
            LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(context);
            localBroadcastManager.sendBroadcast(intent);
        }
    }
}
