package com.example.changfan.ListView.Data;

public class Order implements IData {
    public String id;
    public ClothWithNumber clothWithNumber;
    public double price;
    public String client;
    public String date;
    public String state;

    public Order(String id,ClothWithNumber clothWithNumber,double price,String client,String date,String state){
        this.id=id;
        this.clothWithNumber=clothWithNumber;
        this.price=price;
        this.client=client;
        this.date=date;
        this.state=state;
    }

    //根据字符串生成Order
    public static Order GetOrder(String s){
        //根据斜杠分割字符串
        String[] order=new String[9];
        int count=0;
        int begin=0;
        for(int i=0;i<=s.length()-1;i++){
            if(s.charAt(i)=='/'){
                order[count]=s.substring(begin,i);
                count++;
                begin=i+1;
            }
        }
        order[count]=s.substring(begin);
        return new Order(order[0],new ClothWithNumber(order[1],order[2],Double.parseDouble(order[3]),order[4]),
                Double.parseDouble(order[5]),order[6],order[7],order[8]);
    }
}
