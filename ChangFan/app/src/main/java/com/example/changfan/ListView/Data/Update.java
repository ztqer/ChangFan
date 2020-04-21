package com.example.changfan.ListView.Data;

public class Update implements IData {
    public String orderID;
    public ClothWithNumber clothWithNumber;

    public Update(String orderID,ClothWithNumber clothWithNumber){
        this.orderID=orderID;
        this.clothWithNumber=clothWithNumber;
    }

    //根据字符串生成Update
    public static Update GetUpdate(String s){
        //根据斜杠分割字符串
        String orderid=null;
        String clothwithnumber=null;
        for(int i=0;i<=s.length()-1;i++){
            if(s.charAt(i)=='/'){
                orderid=s.substring(0,i);
                clothwithnumber.substring(i+1);
            }
        }
        return new Update(orderid,ClothWithNumber.GetClothWithNumber(clothwithnumber));
    }
}
