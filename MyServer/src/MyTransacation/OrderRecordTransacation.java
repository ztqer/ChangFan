package MyTransacation;

import java.util.HashMap;
import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;

public class OrderRecordTransacation extends RecordTransacation {
	//订单信息
	private String id;
	private String clothid;
	private String clothcolor;
	private String clothnumber;
	private String price;
	private String client;
	private String date;
	private String state;
	
	//解析客户端发来的字符串
	public OrderRecordTransacation(String data) {
		String[] strings=new String[8];
		int begin=0;
		int index=0;
		for(int i=0;i<=data.length()-2;i++) {
			if(data.charAt(i)=='/') {
				strings[index]=data.substring(begin, i);
				index++;
				begin=i+1;
			}
		}
		strings[index]=data.substring(begin, data.length());
		id=strings[0];
		clothid=strings[1];
		clothcolor=strings[2];
		clothnumber=strings[3];
		price=strings[4];
		client=strings[5];
		date=strings[6];
		state=strings[7];
	}
	
	//根据数据库记录自增补全id编号部分
	@Override
	public void Start() {
		String s=MyServer.jedisPool.getResource().lindex("order", -1);
		String n=String.valueOf(Integer.parseInt(s.substring(8))+1);
		int num=5-n.length();
		for(int i=1;i<=num;i++) {
			n="0"+n;
		}
		id=id+n;
	}

	//向LIST order添加id，向HASH order_具体的id添加其他信息
	@Override
	public void Commit() {
		RedisWriteUnility.Lock();
		RedisWriteUnility.Rpush("order", id);
		HashMap<String, String> hashMap=new HashMap<>();
		hashMap.put("clothid", clothid);
		hashMap.put("clothcolor", clothcolor);
		hashMap.put("clothnumber", clothnumber);
		hashMap.put("price", price);
		hashMap.put("client", client);
		hashMap.put("date", date);
		hashMap.put("state", state);
		RedisWriteUnility.Hmset("order_"+id, hashMap);
		RedisWriteUnility.UnLock();
		result="订单"+id+"存储成功";
		System.out.println(result);
		//广播通知客户端更新
		if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	String s="record/order/"+id+"/"+clothid+"/"+clothcolor+"/"+clothnumber+"/"+price+"/"+client+"/"+date+"/"+state;
            	h.message.add(s);
            }
        }
	}

	//Rollback不起作用
	@Override
	public void Rollback() {}
}
