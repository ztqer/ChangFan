package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import Server.MyServer;
import Server.RedisWriteUnility;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

//登陆
public class LoginHandler extends AbstractHandler {
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		Jedis jedis=MyServer.jedisPool.getResource();
        int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        System.out.println(address+"-收到消息："+s1);
        //从redis的SET username 中查询
    	String s2=jedis.sismember("username", s1)?"验证密码":"不存在此用户";
    	os.write(s2.getBytes());
    	os.flush();
        System.out.println(address+"-发出消息："+s2);
    	if(s2.equals("不存在此用户")) {
    		return;
    	}
        int len2=is.read(buffer);
        String s3=new String(buffer,0,len2);
        System.out.println(address+"-收到消息："+s3);
        //从HASH account_具体的username 中获取密码与权限
        String s4=jedis.hget("account_"+s1, "permission");
    	String s5=s3.equals(jedis.hget("account_"+s1, "password"))?"登录成功"+s4:"密码错误";
    	os.write(s5.getBytes());
    	os.flush();
        System.out.println(address+"-发出消息："+s5);
        if(s5.equals("登录成功"+s4)) {
        	//广播客户端，使此账号已经登陆的下线
        	if(!BroadcastHandler.receivers.isEmpty()) {
                for(BroadcastHandler h:BroadcastHandler.receivers) {
                	h.message.add("warning/"+s1);
                }
            } 
        	//遍历数据时上锁，防止并发隔离性问题
        	RedisWriteUnility.Lock();
        	//从redis读取所有订单和库存信息发送给客户端
        	//从LIST order 读取订单id，再遍历相应的HASH order_具体的id读取其他信息
            LinkedList<String> linkedList1=new LinkedList<>(jedis.lrange("order", 0, -1));
            for(String s_1:linkedList1) {
            	HashMap<String, String> hashMap1=new HashMap<>(jedis.hgetAll("order_"+s_1));
            	String data1=s_1+"/"+hashMap1.get("clothid")+"/"+hashMap1.get("clothcolor")+"/"+hashMap1.get("clothnumber")
            	+"/"+hashMap1.get("clothunit")+"/"+hashMap1.get("price")+"/"+hashMap1.get("client")+"/"+hashMap1.get("date")+"/"+hashMap1.get("state");
            	os.write(data1.getBytes());
            	os.flush();
            	//读一个空字节，防止粘包
            	is.read();
            } 
            String s6="订单传输完成";
            os.write(s6.getBytes());
            os.flush();
            is.read();
            System.out.println(address+"-发出消息："+s6);
            //从HASH clothkind 读取货品id，再遍历相应的HASH clothkind_具体的id获取货品信息、LIST inventory_具体的id获取库存信息
            HashSet<String> hashSet=new HashSet<>(jedis.smembers("clothkind"));
            ArrayList<ArrayList<String>> arrayList1=new ArrayList<>();
            for(String s_2:hashSet) {
            	HashMap<String, String> hashMap2=new HashMap<>(jedis.hgetAll("clothkind_"+s_2));
            	String data2=s_2+"/"+hashMap2.get("weight")+"/"+hashMap2.get("length")+"/"+hashMap2.get("provider")+"/"+hashMap2.get("material");
            	os.write(data2.getBytes());
            	os.flush();
            	is.read();
            	ArrayList<String> arrayList2=new ArrayList<>();
            	LinkedList<String> linkedList2=new LinkedList<>(jedis.lrange("inventory_"+s_2, 0, -1));
            	while(!linkedList2.isEmpty()) {
            		arrayList2.add(s_2+"/"+linkedList2.pop()+"/"+linkedList2.pop()+"/"+linkedList2.pop());
            	}
            	arrayList1.add(arrayList2);
            }
            RedisWriteUnility.UnLock();
            String s7="货品传输完成";
            os.write(s7.getBytes());
            os.flush();
            is.read();
            System.out.println(address+"-发出消息："+s7);
            ObjectOutputStream oos=new ObjectOutputStream(os);
            oos.writeObject(arrayList1);
            oos.flush();
            is.read();
            String s8="库存传输完成";
            os.write(s8.getBytes());
            os.flush();
            is.read();
            System.out.println(address+"-发出消息："+s8);
            jedis.close();
        }
	}
}
