package com.example.changfan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.changfan.BackstageService.RootDialogService;
import com.example.changfan.Handler.LoginHandler;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.Handler.RegisterHandler;

import java.util.ArrayList;

public class LoginActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Button button1,button2;
    private EditText editText1,editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context=this;
        //初始化控件并设置监听
        button1=findViewById(R.id.LoginActivity_button1);
        button2=findViewById(R.id.LoginActivity_button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        editText1=findViewById(R.id.LoginActivity_editText1);
        editText2=findViewById(R.id.LoginActivity_editText2);
    }

    //按钮响应，读取输入信息，尝试登陆与注册
    @Override
    public void onClick(View view) {
        if(editText1.getText().toString().equals("")||editText2.getText().toString().equals("")){
            Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
            return;
        }
        String username=editText1.getText().toString();
        String password=editText2.getText().toString();
        if(view==button1){
            TryLogin(username,password);
            return;
        }
        if(view==button2){
            TryRegister(username,password);
            return;
        }
    }

    //开启线程向服务器验证，成功跳转相应的Activity，失败Toast显示原因
    private void TryLogin(final String username, String password){
        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String s1=msg.getData().getString("result");
                if(s1.equals("登录成功")){
                    String s2=msg.getData().getString("permission");
                    final ArrayList<String> orders=msg.getData().getStringArrayList("orders");
                    final ArrayList<String> clothkinds=msg.getData().getStringArrayList("clothkinds");
                    final ArrayList<ArrayList<String>> inventory=(ArrayList<ArrayList<String>>)msg.getData().getSerializable("inventory");
                    Intent intent=new Intent();
                    if(s2.equals("all")){
                        StartRootService(username);
                        //拥有多权限的弹出对话框选择
                        final String[] activitys = new String[]{"门店", "仓库"};
                        new AlertDialog.Builder(context).setTitle("请选择登陆的角色")
                                .setSingleChoiceItems(activitys, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){
                                            MyStartActivity(new Intent(context,StoreActivity.class),username,orders,clothkinds,inventory);
                                            dialog.dismiss();
                                        }
                                        else if(which==1){
                                            MyStartActivity(new Intent(context,WarehouseActivity.class),username,orders,clothkinds,inventory);
                                            dialog.dismiss();
                                        }
                                    }
                                }).create().show();
                    }
                    else if(s2.equals("warehouse")){
                        MyStartActivity(new Intent(context,WarehouseActivity.class),username,orders,clothkinds,inventory);
                    }
                    else if(s2.equals("store")){
                        MyStartActivity(new Intent(context,StoreActivity.class),username,orders,clothkinds,inventory);
                    }
                }
                else {
                    Toast.makeText(context,s1,Toast.LENGTH_LONG).show();
                }
            }
        };
        Thread tcpThread=new Thread(new TcpThread(new LoginHandler(handler,username,password)));
        tcpThread.start();
    }

    //开启一个tag为root的后台Service，生命周期伴随整个Application，回应注册请求
    private void StartRootService(String username){
        Intent intent=new Intent(context, RootDialogService.class);
        intent.putExtra("tag","root");
        intent.putExtra("username",username);
        startService(intent);
    }
    //开启线程向服务器请求
    private void TryRegister(final String username, String password){
        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String s1=msg.getData().getString("result");
                //让服务器通知root用户
                if(s1.equals("正在请求管理员分配权限，请稍后尝试登陆")){
                    Thread tcpThread1=new Thread(new TcpThread(new OrderHandler("root","有新注册用户，请分配权限:"+username)));
                    tcpThread1.start();
                }
                Toast.makeText(context,s1,Toast.LENGTH_LONG).show();
            }
        };
        Thread tcpThread2=new Thread(new TcpThread(new RegisterHandler(handler,username,password)));
        tcpThread2.start();
    }

    //跳转activity时传递用户名与库存、订单信息
    public void MyStartActivity(Intent intent,String username,ArrayList<String> orders,ArrayList<String> clothkinds,ArrayList<ArrayList<String>> inventory) {
        intent.putExtra("username",username);
        intent.putExtra("orders",orders);
        intent.putExtra("clothkinds",clothkinds);
        intent.putExtra("inventory",inventory);
        startActivity(intent);
    }
}
