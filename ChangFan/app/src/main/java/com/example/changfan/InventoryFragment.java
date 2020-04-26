package com.example.changfan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.ListView.Data.Order;
import com.example.changfan.ListView.Data.Update;
import com.example.changfan.ListView.MyAdapter;
import com.example.changfan.ListView.MyExpandableListAdapter;
import java.util.ArrayList;

public class InventoryFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    //控件
    private Button button1,button2,button3;
    //库存列表
    private ExpandableListView expandableListView1;
    private MyExpandableListAdapter inventoryListAdapter;
    //订单列表
    private ListView listView1;
    private MyAdapter<Order> orderListAdapter;
    //构造从启动intent获取的原始数据，onCreateView中转化为adapter的内容
    private ArrayList<String> clothkinds;
    private ArrayList<ArrayList<String>> inventory;
    private ArrayList<String> orders;

    //内部类的数组，供activity填写数据时提示或限制操作
    public volatile ArrayList<TotalOfClothKind> kinds=new ArrayList<>();
    public static class TotalOfClothKind{
        public String id;
        public ArrayList<NumbersOfColor> colors=new ArrayList<>();
        public TotalOfClothKind(String id){
            this.id=id;
        }
    }
    public static class NumbersOfColor{
        public String color;
        public ArrayList<Double> numbers=new ArrayList<>();
        public double numbercount=0d;
        public NumbersOfColor(String color){
            this.color=color;
        }
    }

    public InventoryFragment(ArrayList<String> orders,ArrayList<String> clothkinds,ArrayList<ArrayList<String>> inventory){
        this.orders=orders;
        this.clothkinds=clothkinds;
        this.inventory=inventory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_inventory,container,false);
        //初始化控件
        button1=view.findViewById(R.id.InventoryFragment_TopMenu_Button1);
        button2=view.findViewById(R.id.InventoryFragment_TopMenu_Button2);
        button3=view.findViewById(R.id.InventoryFragment_TopMenu_Button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button1.setOnLongClickListener(this);
        button2.setOnLongClickListener(this);
        expandableListView1=view.findViewById(R.id.InventoryFragment_MainContent_ExpandableListView1);
        //库存列表
        ArrayList<ClothKind> groupData=new ArrayList<>();
        for(String s1:clothkinds){
            groupData.add(ClothKind.GetClothKind(s1));
        }
        ArrayList<ArrayList<ClothWithNumber>> childData=new ArrayList<>();
        for(ArrayList<String> a:inventory){
            ArrayList<ClothWithNumber> l=new ArrayList<>();
            for(String s2:a){
                l.add(ClothWithNumber.GetClothWithNumber(s2));
            }
            childData.add(l);
        }
        inventoryListAdapter=new MyExpandableListAdapter(groupData,childData,getActivity());
        expandableListView1.setAdapter(inventoryListAdapter);
        //展开组会使其他组子ExpandableListView收回，主动控制使其onGroupCollapse方法能达到
        expandableListView1.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(parent.isGroupExpanded(groupPosition)){
                    parent.collapseGroup(groupPosition);
                }
                else {
                    parent.expandGroup(groupPosition);
                    inventoryListAdapter.CollapseViews();
                }
                return true;
            }
        });
        //订单列表
        ArrayList<Order> data=new ArrayList<>();
        for(String s3:orders){
            data.add(Order.GetOrder(s3));
        }
        listView1=view.findViewById(R.id.InventoryFragment_MainContent_ListView1);
        orderListAdapter=new MyAdapter<>(R.layout.listview_item_order,data,getActivity());
        listView1.setAdapter(orderListAdapter);
        //warehouse初始化需要配货的订单列表
        if(getActivity().getClass()==WarehouseActivity.class){
            WarehouseActivity warehouseActivity=(WarehouseActivity)getActivity();
            for(Order o:data){
                if(o.state.equals("库存中")){
                    warehouseActivity.Refresh(o);
                }
            }
        }
        //实现listview自由滑动,并在滑动时屏蔽viewpager换页
        final Point screenSize=new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
        final MyViewPager viewPager=getActivity().findViewById(R.id.ViewPager);
        listView1.setOnTouchListener(new View.OnTouchListener() {
            private float x,dx;
            private int width=(int)(getActivity().getResources().getDisplayMetrics().density*1350f+0.5f-screenSize.x);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v==listView1){
                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                        x=event.getX();
                        viewPager.canScroll=false;
                        return false;
                    }
                    if(event.getAction()==MotionEvent.ACTION_MOVE){
                        dx=event.getX()-x;
                        x=event.getX();
                        v.scrollBy((int)(-dx),0);
                        if(v.getScrollX()>=width){
                            v.setScrollX(width);
                        }
                        if(v.getScrollX()<=0){
                            v.setScrollX(0);
                            viewPager.canScroll=true;
                        }
                        return false;
                    }
                    if(event.getAction()==MotionEvent.ACTION_UP){
                        viewPager.canScroll=true;
                    }
                }
                return false;
            }
        });
        //初始化publicData数组
        for(ClothKind ck:groupData){
            TotalOfClothKind kind=new TotalOfClothKind(ck.id);
            kinds.add(kind);
            for(ArrayList<ClothWithNumber> al:childData){
                if(al.size()>0 && al.get(0).id.equals(kind.id)){
                    for(ClothWithNumber cwn:al){
                       for(NumbersOfColor color:kind.colors){
                           if(cwn.color.equals(color.color)){
                               color.numbers.add(cwn.number);
                               color.numbercount+=cwn.number;
                               break;
                           }
                       }
                    }
                    break;
                }
            }
        }
        return view;
    }

    //更新列表
    public void Refresh(IData data){
        //添加订单
        if(data.getClass()==Order.class){
            orderListAdapter.AddLine((Order) data);
            return;
        }
        //添加货品
        if(data.getClass()==ClothKind.class){
            ClothKind ck=(ClothKind)data;
            kinds.add(new TotalOfClothKind(ck.id));
            inventoryListAdapter.AddGroup(ck);
            return;
        }
        //添加库存
        if(data.getClass()==ClothWithNumber.class){
            ClothWithNumber cwn=(ClothWithNumber)data;
            //找到组位置并插入
            for(int i=0;i<=inventoryListAdapter.getGroupCount()-1;i++){
                ClothKind ck=(ClothKind)inventoryListAdapter.getGroup(i);
                if(cwn.id.equals(ck.id)){
                    inventoryListAdapter.AddChild(i,cwn);
                    break;
                }
            }
            //更新公开数据
            for(TotalOfClothKind kind:kinds){
                if(cwn.id.equals(kind.id)){
                    for(NumbersOfColor color:kind.colors){
                        if(cwn.color.equals(color.color)){
                            color.numbers.add(cwn.number);
                            color.numbercount+=cwn.number;
                            break;
                        }
                    }
                }
                break;
            }
            return;
        }
        //更新订单和库存
        if(data.getClass()== Update.class){
            Update u=(Update)data;
            ArrayList<Number> a=new ArrayList<>();
            for(Number n:u.numbers){
                a.add(n);
            }
            Update u_=new Update(u.orderId,u.clothWithNumber,a);
            for(int i=0;i<=orderListAdapter.getCount()-1;i++){
                Order o=(Order)orderListAdapter.getItem(i);
                if(u.orderId.equals(o.id)){
                    o.state="发货中";
                    orderListAdapter.notifyDataSetChanged();
                    break;
                }
            }
            //找到子位置并删除
            for(int i=0;i<=inventoryListAdapter.getGroupCount()-1;i++){
                ClothKind ck=(ClothKind)inventoryListAdapter.getGroup(i);
                if(u.clothWithNumber.id.equals(ck.id)){
                    for(int j=0;j<=inventoryListAdapter.childData.get(i).size()-1;j++){
                        ClothWithNumber cwn=inventoryListAdapter.childData.get(i).get(j);
                        if((cwn.color.equals(u.clothWithNumber.color)&&(!u.numbers.isEmpty()))){
                            for(Number n:u.numbers){
                                if(n.number==cwn.number){
                                    u.numbers.remove(n);
                                    inventoryListAdapter.RemoveChild(i,j);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
            //更新公开数据
            for(TotalOfClothKind kind:kinds){
                if(u_.clothWithNumber.id.equals(kind.id)){
                    for(NumbersOfColor color:kind.colors){
                        if((u_.clothWithNumber.color.equals(color.color))&&(!u_.numbers.isEmpty())){
                            for(Number n:u_.numbers){
                                color.numbers.remove(n.number);
                                color.numbercount-=n.number;
                            }
                            break;
                        }
                    }
                }
                break;
            }
            return;
        }
    }

    //上方按钮点击切换显示列表
    @Override
    public void onClick(View v) {
        if(v==button1){
            expandableListView1.setVisibility(View.VISIBLE);
            listView1.setVisibility(View.INVISIBLE);
            return;
        }
        if(v==button2){
            expandableListView1.setVisibility(View.INVISIBLE);
            listView1.setVisibility(View.VISIBLE);
            return;
        }
        //退出筛选视图
        if(v==button3){
            button1.setEnabled(true);
            button2.setEnabled(true);
            button3.setVisibility(View.INVISIBLE);
            listView1.setAdapter(orderListAdapter);
        }
    }

    //长按订单提供筛选功能
    @Override
    public boolean onLongClick(View v) {
        if(v==button2){
            button2.callOnClick();
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setVisibility(View.VISIBLE);
            Dialog dialog=SetDialogContent(button3);
            dialog.show();
            return false;
        }
        return false;
    }

    //设置对话框，选择筛选条件
    private Dialog SetDialogContent(final Button button){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);
        final TextView textView=new TextView(getActivity());
        final EditText editText=new EditText(getActivity());
        linearLayout.addView(textView);
        linearLayout.addView(editText);
        textView.setText("选择类型");
        textView.setTextSize(20);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder _builder=new AlertDialog.Builder(getActivity());
                Dialog _dialog;
                final String[] choices;
                choices=new String[]{"客户","货号","状态"};
                _dialog=_builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText(choices[which]);
                        dialog.dismiss();
                    }
                }).create();
                _dialog.show();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String type=textView.getText().toString();
                String value=editText.getText().toString();
                if(value.equals("")){
                    Toast.makeText(getActivity(),"输入不能为空",Toast.LENGTH_LONG).show();
                    button.callOnClick();
                    return;
                }
                ArrayList<Order> orders=orderListAdapter.GetList();
                ArrayList<Order> newOrders=new ArrayList<>();
                for(Order o:orders){
                    if(type.equals("客户")){
                        if(o.client.equals(value)){
                            newOrders.add(o);
                        }
                    }
                    if(type.equals("货号")){
                        if(o.clothWithNumber.id.equals(value)){
                            newOrders.add(o);
                        }
                    }
                    if(type.equals("状态")){
                        if(o.state.equals(value)){
                            newOrders.add(o);
                        }
                    }
                    listView1.setAdapter(new MyAdapter<Order>(R.layout.listview_item_order,newOrders,getActivity()));
                }
            }
        });
        editText.setTextSize(20);
        return builder.create();
    }
}
