package MyTransacation;

import java.util.HashMap;
import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;

public class ClothKindRecordTransacation extends RecordTransacation {
	//货品信息
	private String id;
	private String weight;
	private String length;
	private String provider;
	private String material;
	
	//解析客户端发来的字符串
	public ClothKindRecordTransacation(String data) {
		String[] strings=new String[5];
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
		weight=strings[1];
		length=strings[2];
		provider=strings[3];
		material=strings[4];
	}
	
	//向SET clothkind添加id，id重复则失败回滚
	@Override
	public void Start() {
		RedisWriteUnility.Lock();
		if(!RedisWriteUnility.Sadd("clothkind", id)) {
			Rollback();
		};
		RedisWriteUnility.UnLock();
	}

	//向HASH clothkind_具体的id添加其他信息
	@Override
	public void Commit() {
		//已回滚则不执行
		if(result!=null) {
			return;
		}
		RedisWriteUnility.Lock();
		HashMap<String, String> hashMap=new HashMap<>();
		hashMap.put("weight", weight);
		hashMap.put("length", length);
		hashMap.put("provider", provider);
		hashMap.put("material", material);
		RedisWriteUnility.Hmset("clothkind_"+id, hashMap);
		RedisWriteUnility.UnLock();
		result="货品"+id+"存储成功";
		System.out.println(result);
		//广播通知客户端更新
		if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	String s="record/clothkind/"+id+"/"+weight+"/"+length+"/"+provider+"/"+material;
            	h.message.add(s);
            }
        }
	}

	@Override
	public void Rollback() {
		result="货品"+id+"存储失败，已存在同名货品";
		System.out.println("货品"+id+"存储失败，已回滚");
	}
}
