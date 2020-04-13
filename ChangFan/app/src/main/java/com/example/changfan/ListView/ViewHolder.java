package com.example.changfan.ListView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.changfan.ListView.Data.Cloth;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.R;

//存储ListView的一行数据
public class ViewHolder<T extends IData>{
    private MyAdapter<T> myAdapter;
    public ViewHolder(MyAdapter<T> myAdapter,View view,T data){
        //获取adapter以便监听器调用方法
        this.myAdapter=myAdapter;
        //根据数据改变内部控件
        ApplyData(view,data);
    }

    //根据view进行相应控件的设置
    private void ApplyData(View view, T data){
        TextView textView;
        switch (view.getId()){
            case R.id.ListViewItem_Text:
                textView=view.findViewById(R.id.ListViewItem_Text_TextView);
                textView.setText(GetText(data));
                break;
            case R.id.ListViewItem_DeletableText:
                textView=view.findViewById(R.id.ListViewItem_DeletableText_TextView);
                textView.setText(GetText(data));
                Button button=view.findViewById(R.id.ListViewItem_DeletableText_Button);
                button.setOnClickListener(new DeleteButtonOnClickListener(myAdapter,data));
                break;
        }
    }

    //反射比较范型，获取数据
    private String GetText(T data){
        if(data.getClass()== Number.class){
            Number n=(Number)data;
            return String.valueOf(n.number);
        }
        if(data.getClass()== Cloth.class){
            Cloth c=(Cloth)data;
            return c.id+" "+c.color;
        }
        if(data.getClass()== ClothWithNumber.class){
            ClothWithNumber cwn=(ClothWithNumber)data;
            return cwn.id+" "+cwn.color+" "+cwn.number;
        }
        return null;
    }

    //按钮监听删除记录
    private class DeleteButtonOnClickListener implements View.OnClickListener{
        private MyAdapter<T> myAdapter;
        private T data;
        private DeleteButtonOnClickListener(MyAdapter<T> myAdapter,T data){
            this.myAdapter=myAdapter;
            this.data=data;
        }
        @Override
        public void onClick(View v) {
            myAdapter.RemoveLine(data);
        }
    }
}
