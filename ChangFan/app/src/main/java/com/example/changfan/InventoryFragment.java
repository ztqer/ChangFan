package com.example.changfan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
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

public class InventoryFragment extends Fragment implements View.OnClickListener {
    //控件
    private Button button1,button2;
    //库存列表
    private ExpandableListView expandableListView1;
    private MyExpandableListAdapter<ClothKind,ClothWithNumber> inventoryListAdapter;
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
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
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
        inventoryListAdapter=new MyExpandableListAdapter<>(groupData,childData,getActivity());
        expandableListView1.setAdapter(inventoryListAdapter);
        //订单列表
        ArrayList<Order> data=new ArrayList<>();
        for(String s3:orders){
            data.add(Order.GetOrder(s3));
        }
        listView1=view.findViewById(R.id.InventoryFragment_MainContent_ListView1);
        orderListAdapter=new MyAdapter<>(R.layout.listview_item_text,data,getActivity());
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
                    for(int j=0;j<=inventoryListAdapter.getChildrenCount(i)-1;j++){
                        ClothWithNumber cwn=(ClothWithNumber) inventoryListAdapter.getChild(i,j);
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
    }
}
