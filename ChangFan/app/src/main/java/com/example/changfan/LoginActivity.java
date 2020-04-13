package com.example.changfan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.changfan.Handler.LoginHandler;

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
        String username=editText1.getText().toString();
        String password=editText2.getText().toString();
        if(view==button1){
            TryLogin(username,password);
        }
        if(view==button2){
            TryRegister(username,password);
        }
    }

    //开启线程向服务器验证，成功跳转相应的Activity，失败Toast显示原因
    private void TryLogin(String username,String password){
        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String s=msg.getData().getString("result");
                if(s.equals("登录成功")){
                    startActivity(new Intent(context,StoreActivity.class));
                }
                else {
                    Toast.makeText(context,msg.getData().getString("string"),Toast.LENGTH_LONG).show();
                }
            }
        };
        Thread tcpThread=new Thread(new TcpThread(new LoginHandler(handler,username,password)));
        tcpThread.start();
    }

    private void TryRegister(String username,String password){

    }
}
