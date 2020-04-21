package com.example.changfan.Handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.changfan.ListView.Data.ClothKind;
import com.example.changfan.ListView.Data.ClothWithNumber;
import com.example.changfan.ListView.Data.Order;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecordHandler extends AbstractHandler {
    private String type;
    private Order order;
    private ClothKind clothKind;
    private ClothWithNumber clothWithNumber;
    private String orderId;
    private OrderHandler orderHandler;
    private Handler handler;
    //重载构造函数，对应不同的请求
    public RecordHandler(Order order, Handler handler){
        type="Order";
        this.order=order;
        this.handler=handler;
    }
    public RecordHandler(ClothKind clothKind, Handler handler){
        type="ClothKind";
        this.clothKind=clothKind;
        this.handler=handler;
    }
    public RecordHandler(ClothWithNumber clothWithNumber, Handler handler){
        type="Inventory";
        this.clothWithNumber=clothWithNumber;
        this.handler=handler;
    }
    public RecordHandler(String orderId,ClothWithNumber clothWithNumber,OrderHandler orderHandler,Handler handler){
        type="Update";
        this.orderId=orderId;
        this.clothWithNumber=clothWithNumber;
        this.orderHandler=orderHandler;
        this.handler=handler;
    }

     @Override
    protected String GetName() {
        return "Record";
    }

    //发送相应类型的信息给服务器
    @Override
    protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        String data=null;
        switch (type){
            case "Order":
                data=type+"/"+order.id+"/"+order.clothWithNumber.id+"/"+order.clothWithNumber.color+"/"+order.clothWithNumber.number
                    +"/"+order.price+"/"+order.client+"/"+order.date+"/"+order.state;
                break;
            case "ClothKind":
                data=type+"/"+clothKind.id+"/"+clothKind.weight+"/"+clothKind.length+"/"+clothKind.provider+"/"+clothKind.material;
                break;
            case "Inventory":
                data=type+"/"+clothWithNumber.id+"/"+clothWithNumber.color+"/"+clothWithNumber.number;
                break;
            case "Update":
                data=type+"/"+orderId+"/"+clothWithNumber.id+"/"+clothWithNumber.color+"/"+clothWithNumber.number;
                break;
        }
        os.write(data.getBytes());
        os.flush();
        int len=is.read(buffer);
        String result=new String(buffer,0,len);
        if(type.equals("Update")&&result.equals("订单"+orderId+"配货失败，不存在相应库存")){
            orderHandler=null;
        }
        //通知主线程显示结果
        Message msg=new Message();
        Bundle b=new Bundle();
        b.putString("result",result);
        msg.setData(b);
        handler.sendMessage(msg);
    }
}
