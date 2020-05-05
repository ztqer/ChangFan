package com.example.changfan.BackstageService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.changfan.Handler.RootHandler;
import com.example.changfan.TcpThread;

public class RootDialogService extends ListenToServerService {
    private String permission="store";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String username=intent.getStringExtra("username");
        //动态注册的BroadcastReceiver随activity销毁,故注册在Service中
        //匿名内部类重写ServiceMessageBroadcastReceiver的功能
        ServiceMessageBroadcastReceiver serviceMessageBroadcastReceiver=new ServiceMessageBroadcastReceiver(this,null,"root",username,null){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                //发出通知栏消息
                if(action.equals("sendNotification")){
                    if(intent.getStringExtra("tag").equals("root")){
                        final String username=intent.getStringExtra("message").substring(13);
                        final String[] activitys = new String[]{"门店", "仓库"};
                        //弹出对话框选择权限
                        Dialog dialog=new AlertDialog.Builder(context).setTitle("请选择该用户的权限:"+username)
                                .setSingleChoiceItems(activitys, 0, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){
                                            permission="store";
                                        }
                                        else if(which==1){
                                            permission="warehouse";
                                        }
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(permission!=null){
                                            Thread tcpThread=new Thread(new TcpThread(new RootHandler(username,permission)));
                                            tcpThread.start();
                                        }
                                    }
                                }).create();
                        //系统级别对话框，不依赖于Activity
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        //API23以上前往系统设置权限
                        if (Build.VERSION.SDK_INT >= 23) {
                            if(!Settings.canDrawOverlays(context)) {
                                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent1);
                                return;
                            } else {
                                dialog.show();
                            }
                        }
                        //Android6.0以下，不用动态声明权限
                        else {
                            dialog.show();
                        }
                    }
                }
            }
        };
        IntentFilter serviceMessageIntentFilter=new IntentFilter("sendNotification");
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageBroadcastReceiver,serviceMessageIntentFilter);
        return super.onStartCommand(intent,flags,startId);
    }
}
