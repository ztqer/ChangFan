package com.example.changfan;

import com.example.changfan.Handler.IHandler;
import java.io.IOException;
import java.net.Socket;

public class TcpThread implements Runnable {
    private static final String test_serverIP="192.168.1.20";//服务器IP地址
    private static final String serverIP="49.234.85.96";//服务器IP地址
    private static final int serverPort=2414;//服务器端口
    private Socket socket;//tcp连接
    private IHandler handler;//要处理的逻辑
    private byte[] buffer = new byte[1024];//缓冲区大小

    //传入任务
    public TcpThread(IHandler h) {
        handler=h;
    }

    //线程执行时异常都上抛到这里
    @Override
    public void run(){
        try {
            socket=new Socket(test_serverIP,serverPort);
            if(handler.HandleMessage(socket.getInputStream(), socket.getOutputStream(), buffer)) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
