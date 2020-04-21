package MyTransacation;

import java.util.ArrayList;
import java.util.Stack;
import Handler.BroadcastHandler;
import Server.MyServer;
import Server.RedisWriteUnility;

public class UpdateRecordTransacation extends RecordTransacation {
	//信息订单号和相应的库存
	private String orderId;
	private String clothId;
	private String color;
	private String number;
	//库存记录在列表中前方元素个数
	private int num;
	
	//解析客户端发来的字符串
	public UpdateRecordTransacation(String data) {
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
		orderId=strings[0];
		clothId=strings[1];
		color=strings[2];
		number=strings[3];
	}
	
	//查询是否存在相应库存
	@Override
	public void Start() {
		boolean b=false;
		ArrayList<String> arrayList=new ArrayList<>(MyServer.jedisPool.getResource().lrange("inventory_"+clothId, 0, -1));
    	for(int i=0;i<=arrayList.size()-2;i++) {
    		if((arrayList.get(i).equals(color)&&arrayList.get(i+1).equals(number))){
    			b=true;
    			num=i;
    			break;
    		}
    	}
    	if(b==false) {
    		Rollback();
    	}
	}

	//向LIST inventory_具体的id 删除相应的库存，并更改HASH order_具体的id state值
	@Override
	public void Commit() {
		//已回滚则不执行
		if(result!=null) {
			return;
		}
		RedisWriteUnility.Lock();
		//将LIST前面部分元素入栈
		Stack<String> stack=new Stack<>();
		for(int i=1;i<=num;i++) {
			stack.push(RedisWriteUnility.Lpop("inventory_"+clothId));
		}
		//删除相应库存
		RedisWriteUnility.Lpop("inventory_"+clothId);
		RedisWriteUnility.Lpop("inventory_"+clothId);
		//将记录重新插入LIST
		while(!stack.isEmpty()) {
			RedisWriteUnility.Lpush("inventory_"+clothId, stack.pop());
		}
		RedisWriteUnility.Hset("order_"+orderId,"state","发货中");
		RedisWriteUnility.UnLock();
		result="订单"+orderId+"配货成功";
		System.out.println(result);
		//广播通知客户端更新
		if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	String s="record/update/"+orderId+"/"+clothId+"/"+color+"/"+number;
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
