package com.example.changfan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.changfan.BackstageService.ListenToServerService;
import com.example.changfan.BackstageService.ServiceMessageBroadcastReceiver;
import com.example.changfan.Handler.IHandler;
import com.example.changfan.ListView.Data.IData;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractActivity extends FragmentActivity {
    //一些activity特有的信息，传递给BroadcastHandler和serviceMessageBroadcastReceiver
    protected Context context;
    protected String tag;
    private String username;
    protected InventoryFragment inventoryFragment;
    //单例线程池
    private ThreadPoolExecutor threadPool=new ThreadPoolExecutor(1,1,0L, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
    //对后台Service的广播接收
    private LocalBroadcastManager localBroadcastManager;
    private ServiceMessageBroadcastReceiver serviceMessageBroadcastReceiver;
    private IntentFilter serviceMessageIntentFilter;
    //用于子线程显示服务器通信结果
    protected Handler simplyDialogShowHandler;
    private Dialog simplyDialog;

    //销毁时停止Service与BroadcastReceiver，防止内存泄露
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, ListenToServerService.class));
        localBroadcastManager.unregisterReceiver(serviceMessageBroadcastReceiver);
    }

    private void StartBackstageService(){
        //注册对Service本地广播的BroadcastReceiver
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        serviceMessageBroadcastReceiver=new ServiceMessageBroadcastReceiver(context,this,tag,username,inventoryFragment);
        serviceMessageIntentFilter=new IntentFilter();
        serviceMessageIntentFilter.addAction("reLogin");
        serviceMessageIntentFilter.addAction("sendNotification");
        serviceMessageIntentFilter.addAction("update");
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

    //子类必须重写
    protected void Initialize(int baseLayout,String tag){
        context=this;
        username=getIntent().getStringExtra("username");
        this.tag=tag;
        simplyDialogShowHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String message=msg.getData().getString("result");
                //多个消息只显示一个对话框
                if(simplyDialog==null){
                    simplyDialog=new AlertDialog.Builder(context).setMessage(message)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    simplyDialog=null;
                                }
                            })
                            .create();
                    simplyDialog.show();
                }
            }
        };
        //初始化ViewPager并把库存的Fragment和当前activity一同放入其中
        final ViewPager viewPager=findViewById(R.id.ViewPager);
        final ArrayList<Object> arrayList=new ArrayList<>();
        arrayList.add(findViewById(baseLayout));
        //读取Intent信息提供数据给inventoryFragment
        ArrayList<String> orders=getIntent().getStringArrayListExtra("orders");
        ArrayList<String> clothkinds=getIntent().getStringArrayListExtra("clothkinds");
        ArrayList<ArrayList<String>> inventory=(ArrayList<ArrayList<String>>)getIntent().getSerializableExtra("inventory");
        inventoryFragment=new InventoryFragment(orders,clothkinds,inventory);
        arrayList.add(inventoryFragment);
        getSupportFragmentManager().beginTransaction().add((InventoryFragment) arrayList.get(1),null).commit();
        PagerAdapter adapter=new PagerAdapter() {
            @Override
            public int getCount() {
                return arrayList.size();
            }
            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                if(object.getClass()==InventoryFragment.class){
                    return ((Fragment)object).getView() == view;
                }
                return view==object;
            }
            //必须与container建立联系，才能实现显示与滑动
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if(position==1){
                    container.addView(((Fragment)arrayList.get(position)).getView());
                }
                else {
                    container.addView((View) arrayList.get(position));
                }
                return arrayList.get(position);
            }
        };
        viewPager.setAdapter(adapter);
        StartBackstageService();
    }

    //接收广播后刷新，由子类重写扩展功能
    public abstract void Refresh(IData data);
}
