package com.example.changfan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.Handler.RecordHandler;
import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.ListView.Data.Order;
import com.example.changfan.ListView.ListViewFragment;
import com.example.changfan.ListView.MyAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class WarehouseActivity extends AbstractActivity implements View.OnClickListener{
    //主界面控件
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView mainContent_editText1;
    private Button mainContent_button1,mainContent_button2;
    //fragment相关
    private ListViewFragment<Number> nowFragment;
    private HashMap<ClothWithNumber,ListViewFragment<Number>> fragmentHashMap=new HashMap<>();
    //左侧滑菜单
    private ArrayList<ClothWithNumber> orderList=new ArrayList<>();
    private HashMap<ClothWithNumber,String> orderid=new HashMap<>();
    private ListView leftMemu_listView;
    private MyAdapter<ClothWithNumber> leftMenu_myAdapter;
    //右侧滑菜单
    private Button rightMenu_button1,rightMenu_button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse);
        Initialize(R.id.WarehouseActivity,"warehouse");
    }

    //初始化控件
    @Override
    protected void Initialize(int baseLayout,String tag){
        super.Initialize(baseLayout,tag);
        //解决滑动冲突
        drawerLayout=findViewById(R.id.WarehouseActivity);
        MyViewPager myViewPager=findViewById(R.id.ViewPager);
        drawerLayout.addDrawerListener(new MyViewPager.MyDrawerListener(myViewPager));
        //主内容界面
        mainContent_editText1=findViewById(R.id.WarehouseActivity_MainContent_EditText1);
        mainContent_editText1.setOnClickListener(this);
        mainContent_button1=findViewById(R.id.WarehouseActivity_MainContent_Button1);
        mainContent_button2=findViewById(R.id.WarehouseActivity_MainContent_Button2);
        mainContent_button1.setOnClickListener(this);
        mainContent_button2.setOnClickListener(this);
        //左侧滑菜单
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
        //右侧滑菜单
        rightMenu_button1=findViewById(R.id.WarehouseActivity_RightMenu_Button1);
        rightMenu_button2=findViewById(R.id.WarehouseActivity_RightMenu_Button2);
        rightMenu_button1.setOnClickListener(this);
        rightMenu_button2.setOnClickListener(this);
    }

    //收到订单信息后解析并更新
    @Override
    public void Refresh(IData data) {
        if(data.getClass()==Order.class){
            Order o=(Order)data;
            //添加到侧滑菜单
            ClothWithNumber cwn=o.clothWithNumber;
            leftMenu_myAdapter.AddLine(cwn);
            orderid.put(cwn,o.id);
            return;
        }
    }

    @Override
    public void onClick(View v) {
        //根据当前货号设置数量自动提醒
        if(v==mainContent_editText1){
            ClothWithNumber cwn=(ClothWithNumber) nowFragment.representative;
            for(InventoryFragment.TotalOfClothKind kind:inventoryFragment.kinds){
                if(kind.id.equals(cwn.id)){
                    for(InventoryFragment.NumbersOfColor color:kind.colors){
                        if(color.color.equals(cwn.color)){
                            mainContent_editText1.setAdapter(new ArrayAdapter<Double>(context,android.R.layout.simple_dropdown_item_1line,color.numbers));
                            break;
                        }
                    }
                    break;
                }
            }
            return;
        }
        //添加一匹布的数量
        if(v==mainContent_button1){
            if(nowFragment==null){
                Toast.makeText(context,"请从侧滑菜单选中一个订单",Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Number n=new Number(Double.parseDouble(mainContent_editText1.getText().toString()));
                nowFragment.AddLine(n);
                mainContent_editText1.setText("");
            }
            catch (NumberFormatException e){
                Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
            }
            return;
        }
        //完成一种布的配货
        if(v==mainContent_button2){
            ClothWithNumber cwn=(ClothWithNumber) nowFragment.representative;
            String message=orderid.get(cwn)+" "+cwn.id+" "+cwn.color+" "+cwn.number+" 已完成配货";
            OrderHandler orderHandler=new OrderHandler("store",message);
            //先尝试更改库存，成功则广播完成配货的消息
            Connect(new RecordHandler(orderid.get(cwn),cwn,orderHandler,simplyDialogShowHandler));
            if(orderHandler!=null){
                Connect(orderHandler);
                //删除fragment并移除相应的orderList元素
                leftMenu_myAdapter.RemoveLine((ClothWithNumber) nowFragment.representative);
                getSupportFragmentManager().beginTransaction().remove(nowFragment).commit();
                nowFragment=null;
            }
            return;
        }
        //增加货品或库存的记录
        if(v==rightMenu_button1){
            Dialog dialog1=setDialogContent("增加新货品",new String[]{"名字","克重","门幅","供货方","材料"}, ClothKind.class);
            dialog1.show();
            return;
        }
        if(v==rightMenu_button2){
            Dialog dialog2=setDialogContent("增加新库存",new String[]{"货品","颜色","数量"},ClothWithNumber.class);
            dialog2.show();
            return;
        }
    }

    //动态设置对话框内容
    private Dialog setDialogContent(String title, String[] strings, final Class type){
        final AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(title);
        //创建一个LinerLayout存初始值为String数组内容的EditText，作为Dialog的View
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ArrayList<EditText> editTexts=new ArrayList<>();
        for(String s:strings){
            final EditText editText=new EditText(context);
            editText.setText(s);
            //触摸后清空初始内容
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    editText.setText("");
                    return false;//返回false继续OnTouchEvent
                }
            });
            editTexts.add(editText);
            linearLayout.addView(editText);
        }
        builder.setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder sb=new StringBuilder();
                //拼接所有EditText信息，根据类型使用RecordHandler通信服务器
                for(EditText et:editTexts){
                    sb.append(et.getText().toString()+"/");
                }
                sb.deleteCharAt(sb.length()-1);
                try{
                    if(type==ClothKind.class){
                        ClothKind ck=ClothKind.GetClothKind(sb.toString());
                        Connect(new RecordHandler(ck,simplyDialogShowHandler));
                    }
                    if(type==ClothWithNumber.class){
                        ClothWithNumber cwn=ClothWithNumber.GetClothWithNumber(sb.toString());
                        Connect(new RecordHandler(cwn,simplyDialogShowHandler));
                    }
                }catch (Exception e){
                    Toast.makeText(context,"输入格式不正确",Toast.LENGTH_LONG).show();
                }
            }
        });
        return builder.create();
    }
}
