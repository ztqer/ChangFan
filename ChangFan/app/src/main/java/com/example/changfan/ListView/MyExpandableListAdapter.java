package com.example.changfan.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.R;
import java.util.ArrayList;

public class MyExpandableListAdapter<T1 extends IData,T2 extends IData> extends BaseExpandableListAdapter {
    private ArrayList<T1> groupData;
    private ArrayList<ArrayList<T2>>  childData;
    private Context context;

    //构造传递数据和context进来
    public MyExpandableListAdapter(ArrayList<T1> groupData, ArrayList<ArrayList<T2>> childData, Context context){
        this.groupData=groupData;
        this.childData=childData;
        this.context=context;
    }

    public void AddGroup(T1 data){
        groupData.add(data);
        notifyDataSetChanged();
    }

    public void AddChild(int groupPosition,T2 data){
        childData.get(groupPosition).add(data);
        notifyDataSetChanged();
    }

    public void RemoveChild(int groupPosition,int childPosition){
        childData.get(groupPosition).remove(childPosition);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //convertView是上一次inflate结果的缓存，复用其节省加载性能消耗
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_text, parent, false);
        }
        convertView.setPadding(100,0,0,0);
        //创建ViewHolder并绑定该层View
        viewHolder=new ViewHolder(null,convertView,groupData.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_text, parent, false);
        }
        viewHolder=new ViewHolder(null,convertView,childData.get(groupPosition).get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
