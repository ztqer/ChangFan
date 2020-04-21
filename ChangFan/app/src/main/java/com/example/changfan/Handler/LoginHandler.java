package com.example.changfan.Handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
            NotifyMainThread(s1,is,os,buffer);
            return;
        }
        int len2=is.read(buffer);
        String s2=new String(buffer,0,len2);
        NotifyMainThread(s2,is,os,buffer);
    }

    //将结果String传递给主线程
    private void NotifyMainThread(String string,InputStream is, OutputStream os, byte[] buffer) throws IOException{
        //拆分字符串获得登录结果与权限
        Message m=new Message();
        Bundle b=new Bundle();
        String s1=string.substring(0,4);
        if(s1.equals("登录成功")){
            String s2=string.substring(4);
            b.putString("result",s1);
            b.putString("permission",s2);
            //登陆成功继续接收库存信息
            ArrayList<String> orders=new ArrayList<>();
            ArrayList<String> clothkinds=new ArrayList<>();
            ArrayList arrayList=orders;
            while(true){
                int len=is.read(buffer);
                if(len==0){
                    continue;
                }
                String s=new String(buffer,0,len);
                if(s.equals("订单传输完成")){
                    arrayList=clothkinds;
                }
                else if(s.equals("货品传输完成")){
                    os.write(new byte[1]);
                    os.flush();
                    break;
                }
                else {
                    arrayList.add(s);
                }
                //写一个空字节，防止粘包
                os.write(new byte[1]);
                os.flush();
            }
            ObjectInputStream ois=new ObjectInputStream(is);
            ArrayList<ArrayList<String>> inventory=null;
            try {
                inventory=(ArrayList<ArrayList<String>>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            os.write(new byte[1]);
            os.flush();
            is.read(buffer);
            b.putStringArrayList("orders",orders);
            b.putStringArrayList("clothkinds",clothkinds);
            b.putSerializable("inventory",inventory);
        }
        else {
            b.putString("result",string);
        }
        m.setData(b);
        mainThreadHandler.sendMessage(m);
    }
}
