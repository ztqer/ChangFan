package com.example.changfan.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.example.changfan.ListView.Data.IData;
import java.util.ArrayList;

public class MyAdapter<T extends IData> extends BaseAdapter {
    private int listview_item;
    private ArrayList<T> list;
    private Context context;

    //构造方法，传递ListView行布局的id、数据列表list和context进来
    public MyAdapter(int listview_item,ArrayList<T> list,Context context){
        this.listview_item=listview_item;
        this.list=list;
        this.context=context;
    }

    //添加一行记录
    public void AddLine(T data){
        list.add(data);
        notifyDataSetChanged();
    }

    //删除一行记录
    public void RemoveLine(int index){
        if(index<0||index>list.size()-1){
            Toast.makeText(context,"删除错误：不存在该元素",Toast.LENGTH_LONG).show();
        }
        else {
            list.remove(index);
            notifyDataSetChanged();
        }
    }
    public void RemoveLine(T data){
        if(!list.remove(data)){
            Toast.makeText(context,"删除错误：不存在该元素",Toast.LENGTH_LONG).show();
        }
        else {
            notifyDataSetChanged();
        }
    }

    //清空列表，调用者可以获取最后的数据列表以使用
    public ArrayList<T> Clear(){
        ArrayList<T> arrayList=(ArrayList<T>)list.clone();//需要拷贝而非引用
        list.clear();
        notifyDataSetChanged();
        return arrayList;
    }

    public ArrayList<T> GetList(){
        return list;
    }

    //创建一层的view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //convertView是上一次inflate结果的缓存，复用其节省加载性能消耗
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(listview_item, parent, false);
        }
        //创建ViewHolder并绑定该层View
        viewHolder=new ViewHolder(this,convertView,list.get(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
