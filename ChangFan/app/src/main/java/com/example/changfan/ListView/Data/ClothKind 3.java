package com.example.changfan.ListView.Data;

public class ClothKind implements IData {
    public String id;
    public double weight;
    public double length;
    public String provider;
    public String material;

    public ClothKind(String id,double weight,double length,String provider,String material){
        this.id=id;
        this.weight=weight;
        this.length=length;
        this.provider=provider;
        this.material=material;
    }

    //根据字符串生成ClothKind
    public static ClothKind GetClothKind(String s){
        //根据斜杠分割字符串
        String[] clothKind=new String[5];
        int count=0;
        int begin=0;
        for(int i=0;i<=s.length()-1;i++){
            if(s.charAt(i)=='/'){
                clothKind[count]=s.substring(begin,i);
                count++;
                begin=i+1;
            }
        }
        clothKind[count]=s.substring(begin);
        return new ClothKind(clothKind[0],Double.parseDouble(clothKind[1]),Double.parseDouble(clothKind[2]),clothKind[3],clothKind[4]);
    }
}
