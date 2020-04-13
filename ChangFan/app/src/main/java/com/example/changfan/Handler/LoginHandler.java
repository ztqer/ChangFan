package com.example.changfan.Handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginHandler extends AbstractHandler{
    private Handler mainThreadHandler;
    private String username,password;
    //构造接收1.Handler通知主线程 2.用户名与密码
    public LoginHandler(Handler mainThreadHandler,String username, String password){
        this.mainThreadHandler=mainThreadHandler;
        this.username=username;
        this.password=password;
    }

    @Override
    protected String GetName() {
        return "Login";
    }

    //依次验证用户名和密码
    @Override
    protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        os.write(username.getBytes());
        os.flush();
        int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        if(s1.equals("验证密码")){
            os.write(password.getBytes());
            os.flush();
        }
        else {
            NotifyMainThread("不存在此用户");
            return;
        }
        int len2=is.read(buffer);
        String s2=new String(buffer,0,len2);
        NotifyMainThread(s2);
    }

    //将结果String传递给主线程
    private void NotifyMainThread(String s){
        Message m=new Message();
        Bundle b=new Bundle();
        b.putString("result",s);
        m.setData(b);
        mainThreadHandler.sendMessage(m);
    }
}
