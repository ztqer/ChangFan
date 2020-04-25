package com.example.changfan.ListView.Data;

import java.util.ArrayList;

public class Update implements IData {
    public String orderId;
    public ClothWithNumber clothWithNumber;
    public ArrayList<Number> numbers;

    public Update(String orderId,ClothWithNumber clothWithNumber,ArrayList<Number> numbers){
        this.orderId=orderId;
        this.clothWithNumber=clothWithNumber;
        this.numbers=numbers;
    }

    //根据字符串生成Update
    public static Update GetUpdate(String s){
        //根据斜杠分割字符串
        ArrayList<String> arrayList=new ArrayList<>();
        int begin=0;
        for(int i=0;i<=s.length()-2;i++) {
            if(s.charAt(i)=='/') {
                arrayList.add(s.substring(begin, i));
                begin=i+1;
            }
        }
        arrayList.add(s.substring(begin));
        String orderId=arrayList.get(0);
        String clothId=arrayList.get(1);
        String color=arrayList.get(2);
        String unit=arrayList.get(3);
        ArrayList<Number> numbers=new ArrayList<>();
        double ds=0d;
        for(int i=4;i<=arrayList.size()-1;i++) {
            double d=Double.parseDouble(arrayList.get(i));
            numbers.add(new Number(d));
            ds+=d;
        }
        return new Update(orderId,new ClothWithNumber(clothId,color,ds,unit),numbers);
    }
}
