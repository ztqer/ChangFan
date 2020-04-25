package com.example.changfan.ListView.Data;

public class ClothWithNumber implements IData{
    public String id;
    public String color;
    public double number;
    public String unit;

    public ClothWithNumber(String id,String color,double number,String unit){
        this.id=id;
        this.color=color;
        this.number=number;
        this.unit=unit;
    }

    //根据字符串生成ClothKind
    public static ClothWithNumber GetClothWithNumber(String s){
        //根据斜杠分割字符串
        String[] clothWithNumber=new String[4];
        int count=0;
        int begin=0;
        for(int i=0;i<=s.length()-1;i++){
            if(s.charAt(i)=='/'){
                clothWithNumber[count]=s.substring(begin,i);
                count++;
                begin=i+1;
            }
        }
        clothWithNumber[count]=s.substring(begin);
        return new ClothWithNumber(clothWithNumber[0],clothWithNumber[1],Double.parseDouble(clothWithNumber[2]),clothWithNumber[3]);
    }
}
