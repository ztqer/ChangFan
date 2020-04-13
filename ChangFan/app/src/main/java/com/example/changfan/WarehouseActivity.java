package com.example.changfan;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.ListView.ListViewFragment;
import com.example.changfan.ListView.MyAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class WarehouseActivity extends AbstractActivity implements View.OnClickListener {
    //主界面控件
    private DrawerLayout drawerLayout;
    private EditText editText1;
    private Button button1,button2;
    //fragment相关
    private ListViewFragment<Number> nowFragment;
    private HashMap<ClothWithNumber,ListViewFragment<Number>> fragmentHashMap=new HashMap<>();
    //侧滑菜单
    private ArrayList<ClothWithNumber> orderList=new ArrayList<>();
    private ListView leftMemu_listView;
    private MyAdapter<ClothWithNumber> leftMenu_myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse);
        Initialize();
    }

    //初始化控件
    @Override
    protected void Initialize(){
        tag="warehouse";
        super.Initialize();
        //主内容界面
        drawerLayout=findViewById(R.id.WarehouseActivity);
        editText1=findViewById(R.id.WarehouseActivity_MainContent_EditText1);
        button1=findViewById(R.id.WarehouseActivity_MainContent_Button1);
        button2=findViewById(R.id.WarehouseActivity_MainContent_Button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        //侧滑菜单
        leftMemu_listView=findViewById(R.id.WarehouseActivity_LeftMenu_ListView1);
        leftMenu_myAdapter=new MyAdapter<>(R.layout.listview_item_text,orderList,context);
        leftMemu_listView.setAdapter(leftMenu_myAdapter);
        //匿名内部类监听侧滑菜单子控件点击
        leftMemu_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //通过HashMap获取并显示fragment，取不到则新建
                ListViewFragment<Number> fragment=fragmentHashMap.get(orderList.get(position));
                if(fragment!=null){
                    getSupportFragmentManager().beginTransaction().hide(nowFragment).show(fragment).commit();
                }
                else {
                    fragment=new ListViewFragment<>(R.layout.listview_item_deletabletext,orderList.get(position));
                    fragmentHashMap.put(orderList.get(position),fragment);
                    if(nowFragment!=null){
                        getSupportFragmentManager().beginTransaction().hide(nowFragment).add(R.id.WarehouseActivity_MainContent_LinearLayout1,fragment).commit();
                    }
                    else {
                        getSupportFragmentManager().beginTransaction().add(R.id.WarehouseActivity_MainContent_LinearLayout1,fragment).commit();
                    }
                }
                nowFragment=fragment;
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    //收到订单信息后解析并更新
    @Override
    public void Refresh(String message) {
        //根据空格分割字符串
        String[] s=new String[3];
        int count=0;
        int begin=0;
        for(int i=0;i<=message.length()-1;i++){
            if(message.charAt(i)==' '){
                s[count]=message.substring(begin,i);
                count++;
                begin=i+1;
            }
        }
        System.out.println(s[0]);
        System.out.println(s[1]);
        System.out.println(s[2]);
        //添加到侧滑菜单
        leftMenu_myAdapter.AddLine(new ClothWithNumber(s[0],s[1],Double.parseDouble(s[2])));
    }

    @Override
    public void onClick(View v) {
        //添加一匹布的数量
        if(v==button1){
            if(nowFragment==null){
                Toast.makeText(context,"请从侧滑菜单选中一个订单",Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Number n=new Number(Double.parseDouble(editText1.getText().toString()));
                nowFragment.AddLine(n);
                editText1.setText("");
            }
            catch (NumberFormatException e){
                Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
            }
            return;
        }
        //完成一种布的配货
        if(v==button2){
            ClothWithNumber cwn=(ClothWithNumber) nowFragment.representative;
            String message=cwn.id+" "+cwn.color+" "+cwn.number+" 已完成配货";
            Connect(new OrderHandler("store",message));
            //删除fragment并移除相应的orderList元素
            leftMenu_myAdapter.RemoveLine((ClothWithNumber) nowFragment.representative);
            getSupportFragmentManager().beginTransaction().remove(nowFragment).commit();
            nowFragment=null;
        }
    }
}
