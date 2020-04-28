package com.example.changfan.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.ClothWithNumber_Count;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.R;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    private ArrayList<ClothKind> groupData;
    private ArrayList<ArrayList<ClothWithNumber>> childCount;
    public ArrayList<ArrayList<ClothWithNumber>> childData;
    private Context context;
    private LinkedList<ExpandableListView> expandedViews=new LinkedList<>();
    private LinkedList<ChildExpanableListAdapter> childrenAdapters=new LinkedList<>();

    //构造传递数据和context进来
    public MyExpandableListAdapter(ArrayList<ClothKind> groupData, ArrayList<ArrayList<ClothWithNumber>> childData, Context context){
        this.groupData=groupData;
        this.childData=childData;
        childCount=new ArrayList<>();
        for(int i=0;i<=childData.size()-1;i++) {
            ArrayList<ClothWithNumber> a1 = childData.get(i);
            ArrayList<ClothWithNumber> a2 = new ArrayList<>();
            childCount.add(a2);
            for(ClothWithNumber cwn1:a1){
                if(a2.isEmpty()){
                    //必须new否则传递引用出错
                    a2.add(new ClothWithNumber(cwn1.id,cwn1.color,cwn1.number,cwn1.unit));
                    continue;
                }
                boolean isNew=true;
                for(ClothWithNumber cwn2:a2){
                    if(cwn2.id.equals(cwn1.id)&&cwn2.color.equals(cwn1.color)&&cwn2.unit.equals(cwn1.unit)){
                        cwn2.number+=cwn1.number;
                        isNew=false;
                        break;
                    }
                }
                if(isNew){
                    a2.add(new ClothWithNumber(cwn1.id,cwn1.color,cwn1.number,cwn1.unit));
                }
            }
        }
        this.context=context;
    }

    //对外封装的几个方法，视childData为子元素
    public void AddGroup(ClothKind data){
        groupData.add(data);
        childCount.add(new ArrayList<ClothWithNumber>());
        childData.add(new ArrayList<ClothWithNumber>());
        MyNotifyDataSetChanged();
    }

    public void AddChild(int groupPosition,ClothWithNumber data){
        if(childCount.get(groupPosition).isEmpty()){
            childCount.get(groupPosition).add(data);
        }
        else {
            for(ClothWithNumber cwn:childCount.get(groupPosition)){
                if(cwn.id.equals(data.id)&&cwn.color.equals(data.color)&&cwn.unit.equals(data.unit)){
                    cwn.number+=data.number;
                    break;
                }
            }
        }
        childData.get(groupPosition).add(data);
        MyNotifyDataSetChanged();
    }

    public void RemoveChild(int groupPosition,int childPosition){
        ClothWithNumber data=childData.get(groupPosition).get(childPosition);
        for(ClothWithNumber cwn:childCount.get(groupPosition)){
            if(cwn.id.equals(data.id)&&cwn.color.equals(data.color)&&cwn.unit.equals(data.unit)){
                cwn.number-=data.number;
                if(cwn.number==0d){
                    childCount.get(groupPosition).remove(cwn);
                }
                break;
            }
        }
        childData.get(groupPosition).remove(childPosition);
        MyNotifyDataSetChanged();
    }

    //通知子ExpandableView一同更新
    private void MyNotifyDataSetChanged(){
        notifyDataSetChanged();
        for(ChildExpanableListAdapter adapter:childrenAdapters){
            adapter.notifyDataSetChanged();
        }
    }

    //重写的几个方法，直接影响getXXXView，以childCount为子元素
    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childCount.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childCount.get(groupPosition).get(childPosition);
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_text, parent, false);
        }
        convertView.setPadding(100,0,0,0);
        viewHolder=new ViewHolder(null,convertView,groupData.get(groupPosition));
        return convertView;
    }

    //嵌套ExpandableView
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        if(convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.listview_item_expandablelistview, parent, false);
        }
        final ExpandableListView expandableListView=convertView.findViewById(R.id.ListViewItem_ExpandableListView_ExpandableListView);
        final ChildExpanableListAdapter adapter=new ChildExpanableListAdapter(childCount.get(groupPosition).get(childPosition),childData.get(groupPosition));
        expandableListView.setAdapter(adapter);
        childrenAdapters.add(adapter);
        convertView.measure(0,0);
        final int h=convertView.getMeasuredHeight();
        //监听展开和收回，调整高度
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                AdjustHeight(h,expandableListView,adapter,true);
                expandedViews.add(expandableListView);
            }
        });
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                AdjustHeight(h,expandableListView,adapter,false);
                expandedViews.remove(expandableListView);
            }
        });
        return convertView;
    }

    //计算高度并调整容器布局
    private void AdjustHeight(int orginalHeight,ExpandableListView expandableListView,ChildExpanableListAdapter adapter,boolean isAdd){
        ViewGroup.LayoutParams layoutParams=expandableListView.getLayoutParams();
        int count=adapter.getChildrenCount(0);
        int h=orginalHeight*2+expandableListView.getDividerHeight()*(count+1);
        for(int i=0;i<=count-1;i++){
            if(i==count-1){
                adapter.getChildView(0,i,true,null,expandableListView);
            }
            else {
                adapter.getChildView(0,i,false,null,expandableListView);
            }
            h+=adapter.childrenHeight[i];
        }
        if(isAdd){
            layoutParams.height+=h;
        }
        else {
            layoutParams.height-=h;
        }
        expandableListView.setLayoutParams(layoutParams);
    }

    //供外界监听器调用，关闭所有子ExpandableListView，使布局高度调整总能进行
    public void CollapseViews(){
        for(ExpandableListView elv:expandedViews){
            elv.collapseGroup(0);
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public class ChildExpanableListAdapter extends BaseExpandableListAdapter{
        private ClothWithNumber group;
        private ArrayList<ClothWithNumber> child;
        private ArrayList<ClothWithNumber> newChild;
        private int[] childrenHeight;

        public ChildExpanableListAdapter(ClothWithNumber group,ArrayList<ClothWithNumber> child){
            this.group=group;
            this.child=child;
            newChild=new ArrayList<>();
            for(ClothWithNumber cwn2:child){
                if(cwn2.id.equals(group.id)&&cwn2.color.equals(group.color)&&cwn2.unit.equals(group.unit)){
                    newChild.add(cwn2);
                }
            }
            childrenHeight=new int[newChild.size()];
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_text, parent, false);
            }
            convertView.setPadding(100,0,0,0);
            viewHolder=new ViewHolder(null,convertView,new ClothWithNumber_Count(group,newChild.size()));
            return convertView;
        }

        //存储生成的View的高度
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_text, parent, false);
            }
            convertView.setPadding(200,0,0,0);
            viewHolder=new ViewHolder(null,convertView,new Number(newChild.get(childPosition).number));
            convertView.measure(0,0);
            childrenHeight[childPosition]=convertView.getMeasuredHeight();
            return convertView;
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return newChild.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return group;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return newChild.get(childPosition);
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
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
