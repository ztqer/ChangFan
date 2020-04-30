package MyTransacation;

import java.util.ArrayList;
import java.util.Stack;
import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;
import redis.clients.jedis.Jedis;

public class UpdateRecordTransacation extends RecordTransacation {
	//信息订单号和相应的库存
	private String orderId;
	private String clothId;
	private String color;
	private String unit;
	private ArrayList<String> numbers=new ArrayList<>();
	private String numbers_String="";
	private double numberCount=0d;
	//其余的库存记录
	private ArrayList<String> inventoryLeft=new ArrayList<>();
	
	//解析客户端发来的字符串
	public UpdateRecordTransacation(String data) {
		ArrayList<String> arrayList=new ArrayList<>();
		int begin=0;
		for(int i=0;i<=data.length()-2;i++) {
			if(data.charAt(i)=='/') {
				arrayList.add(data.substring(begin, i));
				begin=i+1;
			}
		}
		arrayList.add(data.substring(begin));
		orderId=arrayList.get(0);
		clothId=arrayList.get(1);
		color=arrayList.get(2);
		unit=arrayList.get(3);
		for(int i=4;i<=arrayList.size()-1;i++) {
			numbers.add(arrayList.get(i));
			numbers_String+="/"+arrayList.get(i);
			numberCount+=Double.parseDouble(arrayList.get(i));
		}
	}
	
	//查询相应库存的位置
	@Override
	public void Start() {
		Jedis jedis=MyServer.jedisPool.getResource();
		ArrayList<String> arrayList=new ArrayList<>(jedis.lrange("inventory_"+clothId, 0, -1));
		for(int i=0;i<=arrayList.size()-3;i=i+3) {
    		boolean b=false;
    		if((!numbers.isEmpty())&&(arrayList.get(i).equals(color))&&(arrayList.get(i+2).equals(unit))){
    			for(int j=0;j<=numbers.size()-1;j++) {
    				if(numbers.get(j).equals(arrayList.get(i+1))) {
    					numbers.remove(j);
    					b=true;
    					break;
    				}
    			}
    		}
    		if(b==false){
				inventoryLeft.add(arrayList.get(i));
				inventoryLeft.add(arrayList.get(i+1));
				inventoryLeft.add(arrayList.get(i+2));
			}
    	}
    	//存在匹配不到的库存则回滚
    	if(!numbers.isEmpty()) {
    		Rollback();
    	}
    	jedis.close();
	}

	//向LIST inventory_具体的id 删除相应的库存，并更改HASH order_具体的id state值
	@Override
	public void Commit() {
		//已回滚则不执行
		if(result!=null) {
			return;
		}
		RedisWriteUnility.Lock();
		//删除LIST
		RedisWriteUnility.Del("inventory_"+clothId);
		//再把其余元素重新插入
		for(String s:inventoryLeft) {
			RedisWriteUnility.Rpush("inventory_"+clothId, s);
		}
		//更改订单信息
		RedisWriteUnility.Hset("order_"+orderId,"state","发货中");
		RedisWriteUnility.Hset("order_"+orderId, "clothnumber", String.valueOf(numberCount));
		RedisWriteUnility.UnLock();
		result="订单"+orderId+"配货成功";
		System.out.println(result);
		//广播通知客户端更新
		if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	String s="record/update/"+orderId+"/"+clothId+"/"+color+"/"+unit+numbers_String;
            	h.message.add(s);
            }
        }
	}

	@Override
	public void Rollback() {
		result="订单"+orderId+"配货失败，不存在相应库存";
		System.out.println(orderId+"配货失败，已回滚");
	}
}
