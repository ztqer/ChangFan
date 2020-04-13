package com.example.changfan.ListView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.R;
import java.util.ArrayList;

//包含一个ListView的Fragment
public class ListViewFragment<T extends IData> extends Fragment {
    private ListView listView;
    private MyAdapter<T> myAdapter;
    private ArrayList<T> arrayList;
    private int listview_item;
    public Object representative;

    //构造传入ListView行布局的资源
    public ListViewFragment(int listview_item,Object representative){
        this.listview_item=listview_item;
        this.representative=representative;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.listview_fragment,container,false);
        //初始化控件
        listView=view.findViewById(R.id.ListViewFragment_ListView);
        arrayList=new ArrayList<>();
        myAdapter=new MyAdapter<>(listview_item,arrayList,getActivity());
        listView.setAdapter(myAdapter);
        return view;
    }

    //封装一些方法供调用
    public void AddLine(T data){
        myAdapter.AddLine(data);
    }
}
