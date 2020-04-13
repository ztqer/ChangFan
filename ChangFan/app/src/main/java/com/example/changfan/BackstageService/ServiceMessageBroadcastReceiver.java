package com.example.changfan.BackstageService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.changfan.AbstractActivity;
import com.example.changfan.StoreActivity;
import com.example.changfan.R;

public class ServiceMessageBroadcastReceiver extends BroadcastReceiver {
    private Context context;
    private AbstractActivity activity;
    //构造传递相应的activity
    public ServiceMessageBroadcastReceiver(Context context, AbstractActivity activity){
        this.context=context;
        this.activity=activity;
    }

    //从Intent读取信息并通知activity刷新
    @Override
    public void onReceive(Context context, Intent intent) {
        String tag=intent.getStringExtra("tag");
        SendNotification(tag,intent.getStringExtra("message"));
        activity.Refresh(intent.getStringExtra("message"));
    }

    //发送状态栏通知
    private void SendNotification(String title,String textContext){
        //跳转到MainActivity
        Intent intent=new Intent(context, StoreActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
        NotificationManager notificationManager= (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        //API26以上需要额外创建channel
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("channel_1", "channel_name", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId("channel_1")
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(textContext)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
        } else {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(textContext)
                    .setContentIntent(pendingIntent);
        }
        notificationManager.notify(0,notificationBuilder.build());
    }
}
