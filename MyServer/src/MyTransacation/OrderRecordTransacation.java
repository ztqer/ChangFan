package MyTransacation;

import java.util.HashMap;
import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;
import redis.clients.jedis.Jedis;

public class OrderRecordTransacation extends RecordTransacation {
	//订单信息
	private String id;
	private String clothid;
	private String clothcolor;
	private String clothnumber;
	private String clothunit;
	private String price;
	private String client;
	private String date;
	private String state;
	
	//解析客户端发来的字符串
	public OrderRecordTransacation(String data) {
		String[] strings=new String[9];
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
		clothunit=strings[4];
		price=strings[5];
		client=strings[6];
		date=strings[7];
		state=strings[8];
	}
	
	//根据数据库记录自增补全id编号部分
	@Override
	public void Start() {
		Jedis jedis=MyServer.jedisPool.getResource();
		String s=jedis.lindex("order", -1);
		jedis.close();
		int index=Integer.parseInt(s.substring(8));
		//编号第七位为2表示与上订单为同一批，编号不变
		if(id.charAt(7)=='1') {
			index++;
		}
		String n=String.valueOf(index+1);
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
		hashMap.put("clothunit", clothunit);
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
            	String s="record/order/"+id+"/"+clothid+"/"+clothcolor+"/"+clothnumber+"/"+clothunit+"/"+price+"/"+client+"/"+date+"/"+state;
            	h.message.add(s);
            }
        }
	}

	//Rollback不起作用
	@Override
	public void Rollback() {}
}
