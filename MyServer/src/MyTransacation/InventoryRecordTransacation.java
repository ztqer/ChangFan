package MyTransacation;

import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;
import redis.clients.jedis.Jedis;

public class InventoryRecordTransacation extends RecordTransacation {
	//库存信息（一匹布）
	private String id;
	private String color;
	private String number;
	private String unit;
	
	//解析客户端发来的字符串
	public InventoryRecordTransacation(String data) {
		String[] strings=new String[4];
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
		color=strings[1];
		number=strings[2];
		unit=strings[3];
	}
	
	//防止不存在货号却有库存
	@Override
	public void Start() {
		Jedis jedis=MyServer.jedisPool.getResource();
		if(!jedis.sismember("clothkind", id)) {
			Rollback();
		}
		jedis.close();
	}

	//向LIST inventory_具体的id 插入两条记录，分别代表颜色和数量
	@Override
	public void Commit() {
		//已回滚则不执行
		if(result!=null) {
			return;
		}
		RedisWriteUnility.Lock();
		RedisWriteUnility.Rpush("inventory_"+id, color);
		RedisWriteUnility.Rpush("inventory_"+id, number);
		RedisWriteUnility.Rpush("inventory_"+id, unit);
		RedisWriteUnility.UnLock();
		result="库存"+id+"存储成功";
		System.out.println(result);
		//广播通知客户端更新
		if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	String s="record/inventory/"+id+"/"+color+"/"+number+"/"+unit;
            	h.message.add(s);
            }
        }
	}

	@Override
	public void Rollback() {
		result="库存"+id+"存储失败，不存在这种货号";
		System.out.println("库存"+id+"存储失败，已回滚");
	}
}
