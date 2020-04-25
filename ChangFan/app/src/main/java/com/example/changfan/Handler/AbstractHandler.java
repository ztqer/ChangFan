package com.example.changfan.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public abstract class AbstractHandler implements IHandler {
    //向服务器验证，通过后调用Work方法
    @Override
    public boolean HandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        String s1=GetName();
        os.write(s1.getBytes());
        os.flush();
        int len=is.read(buffer);
        String s2=new String(buffer,0,len);
        if(s2.equals("请继续")){
            Work(is,os,buffer);
        }
        return true;
    }

    //子类设定名字
    protected abstract String GetName();

    //子类实现具体功能
    protected abstract void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException;
}
