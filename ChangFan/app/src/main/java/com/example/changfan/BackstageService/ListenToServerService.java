package com.example.changfan.BackstageService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.example.changfan.Handler.BroadcastHandler;
import com.example.changfan.TcpThread;

public class ListenToServerService extends Service {
    //开启线程在后台监听服务器
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String tag=intent.getStringExtra("tag");
        Thread t=new Thread(new TcpThread(new BroadcastHandler(this,tag)));
        t.start();
        return super.onStartCommand(intent,flags,startId);
    }

    //Service必须重写的方法
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
