package com.example.changfan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.example.changfan.BackstageService.RootDialogService;
import com.example.changfan.Handler.LoginHandler;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.Handler.RegisterHandler;
import com.example.changfan.Handler.UpdateHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LoginActivity extends Activity implements View.OnClickListener {
    private Context context;
    private Button button1,button2;
    private EditText editText1,editText2;
    private CheckBox checkBox1;
    private TextView textView3;
    private ProgressBar progressBar;
    private boolean needRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context=this;
        CheckPermissions();
        //初始化控件并设置监听
        button1=findViewById(R.id.LoginActivity_button1);
        button2=findViewById(R.id.LoginActivity_button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        editText1=findViewById(R.id.LoginActivity_editText1);
        editText2=findViewById(R.id.LoginActivity_editText2);
        checkBox1=findViewById(R.id.LoginActivity_CheckBox1);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView==checkBox1){
                    needRemember=isChecked;
                }
            }
        });
        textView3=findViewById(R.id.LoginActivity_TextView3);
        progressBar=findViewById(R.id.LoginActivity_ProgressBar);
        //读取文件存储的账号密码
        try{
            FileInputStream fis=context.openFileInput("AccountMemento");
            int lenghth = fis.available();
            byte[] buffer = new byte[lenghth];
            fis.read(buffer);
            String s=new String(buffer, "UTF-8");
            String username=null;
            String password=null;
            for(int i=0;i<=s.length()-1;i++){
                if(s.charAt(i)=='\r'){
                    username=s.substring(0,i);
                    password=s.substring(i+2);
                    break;
                }
            }
            if(username!=null&&password!=null){
                editText1.setText(username);
                editText2.setText(password);
                checkBox1.setChecked(true);
            }
        }catch (IOException e){
            //什么都不做
        }
        //获取apk路径赋值给静态字段供使用
        SDFileUtility.oldApkPath=getApplicationInfo().sourceDir;
        //检查与更新版本
        Handler progressMonitor=new Handler(){
            private String length;
            @Override
            public void handleMessage(@NonNull Message msg) {
                //显示版本检测结果
                String s=msg.getData().getString("isNewest");
                if(s!=null){
                    Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
                    return;
                }
                //初始化进度条与文字
                s=msg.getData().getString("length");
                if(s!=null){
                    progressBar.setVisibility(View.VISIBLE);
                    textView3.setVisibility(View.VISIBLE);
                    int i=Integer.parseInt(s);
                    progressBar.setMax(i);
                    length="/"+GetSizeWithUnit(i);
                    textView3.setText("0"+length);
                    return;
                }
                //刷新进度条与文字
                s=msg.getData().getString("len");
                if(s!=null){
                    int i=Integer.parseInt(s);
                    progressBar.setProgress(i);
                    textView3.setText(GetSizeWithUnit(i)+length);
                    return;
                }
                //文件传输完成，合并apk
                s=msg.getData().getString("over");
                if(s!=null){
                    SDFileUtility.PatchApk(this);
                    return;
                }
                //apk合并成功，安装新apk
                s=msg.getData().getString("install");
                if(s!=null){
                    SDFileUtility.InstallApk(context);
                    return;
                }
            }

            //将文件大小从字节换算成合适的单位
            private String GetSizeWithUnit(int i){
                double d=(double)i;
                String unit="B";
                if(d>1024d){
                    d=d/1024d;
                    unit="KB";
                    if(d>1024d){
                        d=d/1024d;
                        unit="MB";
                    }
                }
                DecimalFormat decimalFormat=new DecimalFormat("0.00");
                return decimalFormat.format(d)+unit;
            }
        };
        try {
            UpdateHandler h=new UpdateHandler(getPackageManager().getPackageInfo(getPackageName(),0).versionName,progressMonitor);
            Thread t=new Thread(new TcpThread(h));
            t.start();
        } catch (PackageManager.NameNotFoundException e) {
            //什么都不做
        }
    }

    //检测动态申请权限
    private void CheckPermissions(){
        //读写sd卡文件权限
        int requestCode=0;
        requestCode-=ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestCode-=ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        //根据requestCode推测状态（每个权限开启0，拒绝-1）
        if(requestCode>0){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},requestCode);
        }
        //安装未知来源文件权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!getPackageManager().canRequestPackageInstalls()){
                Uri packageURI = Uri.parse("package:"+getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
                startActivity(intent);
            }
        }
    }

    //权限被拒自动关闭程序,权限更改重启程序
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for(int i:grantResults){
            if(i==PackageManager.PERMISSION_DENIED){
                finish();
            }
        }
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    private void TryLogin(final String username, final String password){
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
                                            MyStartActivity(new Intent(context,StoreActivity.class),username,password,orders,clothkinds,inventory);
                                            dialog.dismiss();
                                        }
                                        else if(which==1){
                                            MyStartActivity(new Intent(context,WarehouseActivity.class),username,password,orders,clothkinds,inventory);
                                            dialog.dismiss();
                                        }
                                    }
                                }).create().show();
                    }
                    else if(s2.equals("warehouse")){
                        MyStartActivity(new Intent(context,WarehouseActivity.class),username,password,orders,clothkinds,inventory);
                    }
                    else if(s2.equals("store")){
                        MyStartActivity(new Intent(context,StoreActivity.class),username,password,orders,clothkinds,inventory);
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
    public void MyStartActivity(Intent intent,String username,String password,ArrayList<String> orders,ArrayList<String> clothkinds,ArrayList<ArrayList<String>> inventory) {
        intent.putExtra("username",username);
        intent.putExtra("orders",orders);
        intent.putExtra("clothkinds",clothkinds);
        intent.putExtra("inventory",inventory);
        //存储账号密码
        try {
            if(needRemember){
                FileOutputStream fis=context.openFileOutput("AccountMemento",MODE_PRIVATE);
                String s=username+"\r\n"+password;
                fis.write(s.getBytes());
                fis.flush();
            }
            else {
                context.deleteFile("AccountMemento");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
