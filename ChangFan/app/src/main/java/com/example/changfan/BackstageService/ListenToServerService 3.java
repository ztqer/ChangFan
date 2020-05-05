package com.example.changfan.BackstageService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.example.changfan.Handler.BroadcastHandler;
import com.example.changfan.TcpThread;

public class ListenToServerService extends Service {
    private Thread thread;
    private BroadcastHandler broadcastHandler;
    //开启线程在后台监听服务器
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String tag=intent.getStringExtra("tag");
        broadcastHandler=new BroadcastHandler(this,tag);
        thread=new Thread(new TcpThread(broadcastHandler));
        thread.start();
        return super.onStartCommand(intent,flags,startId);
    }

    //关闭线程，防止内存泄漏
    @Override
    public void onDestroy() {
        broadcastHandler.isover=true;
        super.onDestroy();
    }

    //Service必须重写的方法
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
