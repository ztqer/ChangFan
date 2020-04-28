package com.example.changfan;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.changfan.Handler.OrderHandler;
import com.example.changfan.Handler.RecordHandler;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Order;
import com.example.changfan.ListView.MyAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StoreActivity extends AbstractActivity implements View.OnClickListener {
    //主界面控件
    private DrawerLayout drawerLayout;
    private EditText mainContent_editText2,leftMenu_editText1,leftMenu_editText3;
    private AutoCompleteTextView mainContent_editText1,leftMenu_editText2;
    private Button mainContent_button1,mainContent_button2,mainContent_button3,leftMenu_button1;
    //Listview相关
    private ListView mainContent_listView1;
    private MyAdapter<ClothWithNumber> myAdapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Initialize(R.id.StoreActivity,"store");
    }

    @Override
    protected void Initialize(int baseLayout,String tag){
        super.Initialize(baseLayout,tag);
        //解决滑动冲突
        drawerLayout=findViewById(R.id.StoreActivity);
        MyViewPager myViewPager=findViewById(R.id.ViewPager);
        drawerLayout.addDrawerListener(new MyViewPager.MyDrawerListener(myViewPager));
        //主内容界面
        mainContent_editText1=findViewById(R.id.StoreActivity_MainContent_EditText1);
        mainContent_editText1.setOnClickListener(this);
        mainContent_editText2=findViewById(R.id.StoreActivity_MainContent_EditText2);
        mainContent_button1=findViewById(R.id.StoreActivity_MainContent_Button1);
        mainContent_button2=findViewById(R.id.StoreActivity_MainContent_Button2);
        mainContent_button3=findViewById(R.id.StoreActivity_MainContent_Button3);
        mainContent_button1.setOnClickListener(this);
        mainContent_button2.setOnClickListener(this);
        mainContent_button3.setOnClickListener(this);
        //ListView相关
        mainContent_listView1=findViewById(R.id.StoreActivity_MainContent_ListView1);
        myAdapter1=new MyAdapter<>(R.layout.listview_item_deletabletext,new ArrayList<ClothWithNumber>(),context);
        mainContent_listView1.setAdapter(myAdapter1);
        //侧滑菜单
        leftMenu_editText1=findViewById(R.id.StoreActivity_LeftMenu_EditText1);
        leftMenu_editText2=findViewById(R.id.StoreActivity_LeftMenu_EditText2);
        Refresh(null);
        leftMenu_editText3=findViewById(R.id.StoreActivity_LeftMenu_EditText3);
        leftMenu_button1=findViewById(R.id.StoreActivity_LeftMenu_Button1);
        leftMenu_button1.setOnClickListener(this);
    }

    //更新AutoCompleteTextView的Adapter
    @Override
    public void Refresh(IData data) {
        ArrayList<String> arrayList=new ArrayList<>();
        for(InventoryFragment.TotalOfClothKind kind:inventoryFragment.kinds){
            arrayList.add(kind.id);
        }
        leftMenu_editText2.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,arrayList));
    }

    @Override
    public void onClick(View v) {
        //根据货号设置颜色的自动提醒
        if(v==mainContent_editText1){
            for(InventoryFragment.TotalOfClothKind kind:inventoryFragment.kinds){
                if(kind.id.equals(leftMenu_editText2.getText().toString())){
                    ArrayList<String> arrayList=new ArrayList<>();
                    for(InventoryFragment.NumbersOfColor color:kind.colors){
                        arrayList.add(color.color);
                    }
                    mainContent_editText1.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,arrayList));
                    break;
                }
            }
            return;
         }
        //添加一个订单
        if(v==mainContent_button1){
            if(leftMenu_editText1.getText().toString().equals("")||leftMenu_editText2.getText().toString().equals("")
                    ||leftMenu_editText3.getText().toString().equals("") ||mainContent_editText1.getText().toString().equals("")
                    ||mainContent_editText2.getText().toString().equals("")){
                Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
            }
            else {
                String id=leftMenu_editText2.getText().toString();
                String color=mainContent_editText1.getText().toString();
                double number=Double.parseDouble(mainContent_editText2.getText().toString());
                for(InventoryFragment.TotalOfClothKind kind:inventoryFragment.kinds){
                    if(kind.id.equals(id)){
                        for(InventoryFragment.NumbersOfColor c:kind.colors){
                            if(c.color.equals(color)&&c.numbercount<number){
                                Toast.makeText(context,"库存不足",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        break;
                    }
                }
                String unit=leftMenu_button1.getText().toString();
                ClothWithNumber cwn1=new ClothWithNumber(id,color,number,unit);
                myAdapter1.AddLine(cwn1);
                mainContent_editText1.setText("");
                mainContent_editText2.setText("");
            }
            return;
        }
        //确认订单并通知仓库
        if(v==mainContent_button2){
            ArrayList<ClothWithNumber> arrayList=myAdapter1.Clear();
            if(arrayList.isEmpty()){
                Toast.makeText(context,"当前列表为空",Toast.LENGTH_LONG).show();
                return;
            }
            //CF（名字缩写）01(门店号)01（仓库号）01（类型标志）00001（编号）
            String id="CF0101";
            String client=leftMenu_editText1.getText().toString();
            double price=Double.parseDouble(leftMenu_editText3.getText().toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            Date d= new Date(System.currentTimeMillis());
            String date=simpleDateFormat.format(d);
            int i=1;
            for(ClothWithNumber cwn2:arrayList){
                if(i<10){
                    id+="0"+String.valueOf(i);
                }
                else {
                    id+=String.valueOf(i);
                }
                Order order=new Order(id,cwn2,price,client,date,"库存中");
                String message=cwn2.id+" "+cwn2.color+" "+cwn2.number+cwn2.unit+" 需要配货";
                Connect(new OrderHandler("warehouse",message));
                Connect(new RecordHandler(order,simplyDialogShowHandler));
            }
            leftMenu_editText1.setText("");
            leftMenu_editText2.setText("");
            leftMenu_editText3.setText("");
            return;
        }
        //展开侧滑菜单
        if(v==mainContent_button3){
            drawerLayout.openDrawer(GravityCompat.START);
            return;
        }
        //点击切换数量单位
        if(v==leftMenu_button1){
            String s=leftMenu_button1.getText().toString().equals("公斤")?"米":"公斤";
            leftMenu_button1.setText(s);
            return;
        }
    }
}
