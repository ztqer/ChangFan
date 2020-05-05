package com.example.changfan.BackstageService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import com.example.changfan.AbstractActivity;
import com.example.changfan.InventoryFragment;
import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Order;
import com.example.changfan.ListView.Data.Update;
import com.example.changfan.LoginActivity;
import com.example.changfan.R;

public class ServiceMessageBroadcastReceiver extends BroadcastReceiver {
    private Context context;
    private AbstractActivity activity;
    private String tag;
    private String username;
    private InventoryFragment inventoryFragment;
    //构造1.Context 2.Activity用于调用刷新与点击通知的跳转 3.tag用于判别 4.username表明用户
    public ServiceMessageBroadcastReceiver(Context context, AbstractActivity activity, String tag, String username, InventoryFragment inventoryFragment){
        this.context=context;
        this.activity=activity;
        this.tag=tag;
        this.username=username;
        this.inventoryFragment=inventoryFragment;
    }

    //从Intent读取信息并通知activity刷新
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        //发出通知栏消息
        if(action.equals("sendNotification")){
            String tag=intent.getStringExtra("tag");
            if(tag.equals(this.tag)){
                SendNotification("昌繁",intent.getStringExtra("message"));
            }
        }
        //返回登陆界面
        if(action.equals("reLogin")){
            String username=intent.getStringExtra("username");
            if(username.equals(this.username)){
                if(activity!=null){
                    Toast.makeText(activity,"您的账号在别处登录",Toast.LENGTH_LONG).show();
                    activity.startActivity(new Intent(context, LoginActivity.class));
                }
            }
        }
        //通知inventoryFragment和activity解析并应用数据
        if(action.equals("update")){
            String data=intent.getStringExtra("data");
            IData iData=JudgeType(data);
            if(iData!=null){
                activity.Refresh(iData);
                inventoryFragment.Refresh(iData);
            }
        }
    }

    //发送状态栏通知
    private void SendNotification(String title,String textContext){
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
                    .setAutoCancel(true);
        } else {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(textContext);
        }

        //跳转到相应Activity
        if(activity!=null){
            Intent intent=new Intent(context, activity.getClass());
            PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
            notificationBuilder.setContentIntent(pendingIntent);
        }
        notificationManager.notify(0,notificationBuilder.build());
    }

    //判断并返回相应的数据类型对象
    private IData JudgeType(String data){
        String type=null;
        String newData=null;
        for(int i=0;i<data.length()-2;i++){
            if(data.charAt(i)=='/'){
                type=data.substring(0,i);
                newData=data.substring(i+1);
                break;
            }
        }
        switch (type){
            case "order":
                return Order.GetOrder(newData);
            case "clothkind":
                return ClothKind.GetClothKind(newData);
            case "inventory":
                return ClothWithNumber.GetClothWithNumber(newData);
            case "update":
                return Update.GetUpdate(newData);
        }
        return null;
    }
}
