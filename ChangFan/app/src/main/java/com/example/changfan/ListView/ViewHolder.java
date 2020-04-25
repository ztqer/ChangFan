package com.example.changfan.ListView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.changfan.ListView.Data.Cloth;
import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.IData;
import com.example.changfan.ListView.Data.Number;
import com.example.changfan.ListView.Data.Order;
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
            case R.id.ListViewItem_Order:
                if(data.getClass()== Order.class){
                    Order o=(Order)data;
                    TextView textView1=view.findViewById(R.id.ListViewItem_Order_TextView1);
                    textView1.setText(o.id);
                    TextView textView2=view.findViewById(R.id.ListViewItem_Order_TextView2);
                    textView2.setText(o.clothWithNumber.id);
                    TextView textView3=view.findViewById(R.id.ListViewItem_Order_TextView3);
                    textView3.setText(o.clothWithNumber.color);
                    TextView textView4=view.findViewById(R.id.ListViewItem_Order_TextView4);
                    String s=o.clothWithNumber.number==0?"数量 ":String.valueOf(o.clothWithNumber.number);
                    textView4.setText(s+o.clothWithNumber.unit);
                    TextView textView5=view.findViewById(R.id.ListViewItem_Order_TextView5);
                    if(o.price==0){
                        textView5.setText("单价");
                    }
                    else {
                        textView5.setText(String.valueOf(o.price));
                    }
                    TextView textView6=view.findViewById(R.id.ListViewItem_Order_TextView6);
                    textView6.setText(o.client);
                    TextView textView7=view.findViewById(R.id.ListViewItem_Order_TextView7);
                    textView7.setText(o.date);
                    TextView textView8=view.findViewById(R.id.ListViewItem_Order_TextView8);
                    textView8.setText(o.state);
                }
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
            return cwn.id+" "+cwn.color+" "+cwn.number+cwn.unit;
        }
        if(data.getClass()== ClothKind.class){
            ClothKind ck=(ClothKind)data;
            return ck.id+" "+ck.weight+"克/平方米 "+ck.length+"米 "+ck.provider+" "+ck.material;
        }
        if(data.getClass()== Order.class){
            Order o=(Order)data;
            return o.id+" "+o.clothWithNumber.id+" "+o.clothWithNumber.color+" "+o.clothWithNumber.number+o.clothWithNumber.unit+" "+ o.price+" "+o.client+" "+o.date+" "+o.state;
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
