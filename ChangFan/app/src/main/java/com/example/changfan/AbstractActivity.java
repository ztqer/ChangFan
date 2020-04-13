package com.example.changfan;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.changfan.BackstageService.ListenToServerService;
import com.example.changfan.BackstageService.ServiceMessageBroadcastReceiver;
import com.example.changfan.Handler.IHandler;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractActivity extends FragmentActivity {
    protected Context context;
    //通过Intent传递给Service创建相应的BroadcastHandler
    protected String tag;

    //单例线程池
    private ThreadPoolExecutor threadPool=new ThreadPoolExecutor(1,1,0L, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
    //对后台Service的广播接收
    private LocalBroadcastManager localBroadcastManager;
    private ServiceMessageBroadcastReceiver serviceMessageBroadcastReceiver;
    private IntentFilter serviceMessageIntentFilter;

    //销毁时停止Service与BroadcastReceiver
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, ListenToServerService.class));
        localBroadcastManager.unregisterReceiver(serviceMessageBroadcastReceiver);
    }

    private void StartBackstageService(){
        //注册对Service本地广播的BroadcastReceiver
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        serviceMessageBroadcastReceiver=new ServiceMessageBroadcastReceiver(context,this);
        serviceMessageIntentFilter=new IntentFilter("sendNotification");
        localBroadcastManager.registerReceiver(serviceMessageBroadcastReceiver,serviceMessageIntentFilter);
        //开启后台Service
        Intent intent=new Intent(context, ListenToServerService.class);
        intent.putExtra("tag",tag);
        startService(intent);
    }

    //执行与服务器通信任务
    protected void Connect(IHandler h){
        threadPool.execute(new TcpThread(h));
    }

    //子类必须重写并在super前赋值tag
    protected void Initialize(){
        context=this;
        StartBackstageService();
    }

    //接收广播后刷新，由子类实现具体功能
    public abstract void Refresh(String message);
}
