package com.example.changfan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.example.changfan.ListView.Data.Update;
import com.example.changfan.ListView.ListViewFragment;
import com.example.changfan.ListView.MyAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class WarehouseActivity extends AbstractActivity implements View.OnClickListener{
    //主界面控件
    private DrawerLayout drawerLayout;
    private AutoCompleteTextView mainContent_editText1;
    private Button mainContent_button1,mainContent_button2,mainContent_button3,mainContent_button4;
    private TextView mainContent_textView1;
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
        mainContent_button3=findViewById(R.id.WarehouseActivity_MainContent_Button3);
        mainContent_button4=findViewById(R.id.WarehouseActivity_MainContent_Button4);
        mainContent_button1.setOnClickListener(this);
        mainContent_button2.setOnClickListener(this);
        mainContent_button3.setOnClickListener(this);
        mainContent_button4.setOnClickListener(this);
        mainContent_textView1=findViewById(R.id.WarehouseActivity_MainContent_TextView1);
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
                mainContent_textView1.setText(orderList.get(position).id+" "+orderList.get(position).color+" "+orderList.get(position).number+orderList.get(position).unit);
            }
        });
        //右侧滑菜单
        rightMenu_button1=findViewById(R.id.WarehouseActivity_RightMenu_Button1);
        rightMenu_button2=findViewById(R.id.WarehouseActivity_RightMenu_Button2);
        rightMenu_button1.setOnClickListener(this);
        rightMenu_button2.setOnClickListener(this);
    }

    //收到信息后解析并更新
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
        if(data.getClass()==Update.class){
            Update u=(Update)data;
            for(ClothWithNumber cwn:orderList){
                if(orderid.get(cwn).equals(u.orderId)){
                    //向门店发出通知
                    String message=orderid.get(cwn)+" "+cwn.id+" "+cwn.color+" "+cwn.number+" 已完成配货";
                    OrderHandler orderHandler=new OrderHandler("store",message);
                    Connect(orderHandler);
                    //删除fragment并移除相应的orderList元素
                    leftMenu_myAdapter.RemoveLine(cwn);
                    ListViewFragment<Number> fragment=fragmentHashMap.get(cwn);
                    if(nowFragment==fragment){
                        nowFragment=null;
                    }
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
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
            if(nowFragment==null||nowFragment.IsEmpty()){
                Toast.makeText(context,"当前列表为空",Toast.LENGTH_LONG).show();
                return;
            }
            //先尝试更改库存，成功则广播完成配货的消息(在Refresh中)
            ClothWithNumber cwn=(ClothWithNumber) nowFragment.representative;
            Update u=new Update(orderid.get(cwn),cwn,nowFragment.Clear());
            Connect(new RecordHandler(u,simplyDialogShowHandler));
            mainContent_textView1.setText("");
            return;
        }
        //展开侧滑菜单
        if(v==mainContent_button3){
            drawerLayout.openDrawer(GravityCompat.START);
            return;
        }
        if(v==mainContent_button4){
            drawerLayout.openDrawer(GravityCompat.END);
            return;
        }
        //增加货品或库存的记录
        if(v==rightMenu_button1){
            Dialog dialog1=SetDialogContent("增加新货品",new String[]{"名字","克重","门幅","供货方","材料"}, ClothKind.class);
            dialog1.show();
            return;
        }
        if(v==rightMenu_button2){
            Dialog dialog2=SetDialogContent("增加新库存",new String[]{"货品","颜色","单位（米/公斤）","数量"},ClothWithNumber.class);
            dialog2.show();
            return;
        }
    }

    //动态设置对话框内容
    private Dialog SetDialogContent(String title, String[] strings, final Class type){
        final AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(title);
        //创建一个LinerLayout存初始值为String数组内容的EditText，并用ScrollView包裹起来作为Dialog的View
        ScrollView scrollView=new ScrollView(context);
        final LinearLayout linearLayout=new LinearLayout(context);
        scrollView.addView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ArrayList<EditText> editTexts=new ArrayList<>();
        for(String s:strings){
            AddMyEditText(context,s,linearLayout,editTexts);
        }
        builder.setView(scrollView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            //获取所有EditText信息，根据type转化为相应数据使用RecordHandler通信服务器
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> arrayList=new ArrayList<>();
                for(EditText et:editTexts){
                    if(et.getText().toString().equals("")){
                        Toast.makeText(context,"输入不能为空",Toast.LENGTH_LONG).show();
                        return;
                    }
                    arrayList.add(et.getText().toString());
                }
                try{
                    if(type==ClothKind.class){
                        ClothKind ck=new ClothKind(arrayList.get(0),Double.parseDouble(arrayList.get(1)),Double.parseDouble(arrayList.get(2)),arrayList.get(3),arrayList.get(4));
                        Connect(new RecordHandler(ck,simplyDialogShowHandler));
                    }
                    if(type==ClothWithNumber.class){
                        //限制单位格式
                        if(!(arrayList.get(2).equals("米")||arrayList.get(2).equals("公斤"))){
                            throw new Exception();
                        }
                        for(int i=3;i<=arrayList.size()-1;i++){
                            ClothWithNumber cwn=new ClothWithNumber(arrayList.get(0),arrayList.get(1),Double.parseDouble(arrayList.get(i)),arrayList.get(2));
                            Connect(new RecordHandler(cwn,simplyDialogShowHandler));
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(context,"输入格式不正确",Toast.LENGTH_LONG).show();
                }
            }
        });
        return builder.create();
    }

    //生成EditText并完成各种设置
    private void AddMyEditText(final Context context, String text, final LinearLayout linearLayout, final ArrayList<EditText> editTexts){
        final EditText editText;
        //货品设置为AutoCompleteTextView（其继承于EditText，不影响别处使用）
        if(text.equals("货品")){
            ArrayList<String> arrayList=new ArrayList<>();
            for(InventoryFragment.TotalOfClothKind kind:inventoryFragment.kinds){
                arrayList.add(kind.id);
            }
            editText=new AutoCompleteTextView(context);
            AutoCompleteTextView actv=(AutoCompleteTextView)editText;
            actv.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,arrayList));
        }
        else {
            editText=new EditText(context);
        }
        editText.setText(text);
        //设置数字格式
        if(text.equals("克重")||text.equals("门幅")||text.equals("数量")){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        //按下确定键生成新的EditText
        if(text.equals("数量")){
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_DONE){
                        AddMyEditText(context,"数量",linearLayout,editTexts);
                    }
                    return false;
                }
            });
        }
        //触摸后清空初始内容
        editText.setOnTouchListener(new View.OnTouchListener() {
            private boolean isFirst=true;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isFirst==true){
                    editText.setText("");
                    isFirst=false;
                }
                return false;//返回false继续OnTouchEvent
            }
        });
        editTexts.add(editText);
        linearLayout.addView(editText);
    }
}
