package com.example.changfan.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RootHandler extends AbstractHandler {
    private String username;
    private String permission;
    //构造接收描述权限的String
    public RootHandler(String username,String permission){
        this.username=username;
        this.permission=permission;
    }

    @Override
    protected String GetName() {
        return "Root";
    }

    @Override
    protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        os.write((username).getBytes());
        os.flush();
        int len=is.read(buffer);
        String s=new String(buffer,0,len);
        if(s.equals("继续")){
            os.write((permission).getBytes());
            os.flush();
        }
    }
}
