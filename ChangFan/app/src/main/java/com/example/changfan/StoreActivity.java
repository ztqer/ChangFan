package com.example.changfan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.MyAdapter;
import java.util.ArrayList;

public class StoreActivity extends AbstractActivity implements View.OnClickListener {
    //控件
    private EditText editText1,editText2,editText3;
    private Button button1,button2;
    //Listview相关
    private ListView listView1;
    private MyAdapter<ClothWithNumber> myAdapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Initialize();
    }

    @Override
    protected void Initialize(){
        tag="store";
        super.Initialize();
        //主内容界面
        editText1=findViewById(R.id.StoreActivity_EditText1);
        editText2=findViewById(R.id.StoreActivity_EditText2);
        editText3=findViewById(R.id.StoreActivity_EditText3);
        button1=findViewById(R.id.StoreActivity_Button1);
        button2=findViewById(R.id.StoreActivity_Button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        //ListView相关
        listView1=findViewById(R.id.StoreActivity_ListView1);
        myAdapter1=new MyAdapter<>(R.layout.listview_item_deletabletext,new ArrayList<ClothWithNumber>(),context);
        listView1.setAdapter(myAdapter1);
    }

    //暂时什么都不做
    @Override
    public void Refresh(String message) {

    }

    @Override
    public void onClick(View v) {
        //添加一个订单
        if(v==button1){
            if(editText1.getText().toString().equals("")||editText2.getText().toString().equals("")||editText3.getText().toString().equals("")){
                Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
            }
            else {
                String id=editText1.getText().toString();
                String color=editText2.getText().toString();
                double number=Double.parseDouble(editText3.getText().toString());
                ClothWithNumber cwn1=new ClothWithNumber(id,color,number);
                myAdapter1.AddLine(cwn1);
                editText1.setText("");
                editText2.setText("");
                editText3.setText("");
                return;
            }
        }
        //通知仓库
        if(v==button2){
            ArrayList<ClothWithNumber> arrayList=myAdapter1.Clear();
            for(ClothWithNumber cwn2:arrayList){
                String message=cwn2.id+" "+cwn2.color+" "+cwn2.number+" 需要配货";
                Connect(new OrderHandler("warehouse",message));
            }
        }
    }
}
