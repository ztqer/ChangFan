package Server;

import java.util.HashMap;
import redis.clients.jedis.params.SetParams;

//静态工具类，对Redis的写操作全部从此处调用
public class RedisWriteUnility {
	//Redis结构描述：
	//账号：
	//SET username value1,value2...
	//HASH account_具体的username password value1 permission value2 
	//订单：
	//LIST order value 订单id
	//HASH order_具体的id clothid value1 clothcolor value2 clothnumber value3 price value4 client value5 date value6 state value7
	//库存
	//SET clothkind value1,value2... 货品id
	//HASH clothkind_具体的id weight value1 length value2 provider value3 material value4
	//LIST inventory_具体的id value1,value2,value3... 每三个值记录一匹布颜色+数量+单位
	
	//分布式锁，向redis插入lock的key，若已存在（setnx返回null）则被其他人占用，过期时间为20s避免宕机客户不能释放锁
	public static void Lock() {
		//自旋直到取得锁
		while(MyServer.jedisPool.getResource().set("lock","using",SetParams.setParams().nx().ex(20))==null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("上锁成功");
	}
	//释放锁
	public static void UnLock() {
		MyServer.jedisPool.getResource().del("lock");
		System.out.println("解锁成功");
	}
	
	//SET已有此元素返回false,已存在不同类型的该key抛出错误
	public static boolean Sadd(String key,String value) {
		Long result=MyServer.jedisPool.getResource().sadd(key, value);
		return result==1L?true:false;
	}
	
	//SET不存在抛出错误
	public static void Srem(String key,String value) {
		MyServer.jedisPool.getResource().srem(key,value);
	}
	
	//hashMap为空抛出异常
	public static void Hmset(String key,HashMap<String, String> hashMap) {
		MyServer.jedisPool.getResource().hmset(key, hashMap);
	}
	
	public static void Hset(String key,String field,String value) {
		MyServer.jedisPool.getResource().hset(key, field, value);
	}
	
	//已存在不同类型的该key抛出错误
	public static void Rpush(String key,String value) {
		MyServer.jedisPool.getResource().rpush(key, value);
	}
	
	//key不存在或LIST为空返回null
	public static String Lpop(String key) {
		return MyServer.jedisPool.getResource().lpop(key);
	}
	
	//已存在不同类型的该key抛出错误
	public static void Lpush(String key,String value) {
		MyServer.jedisPool.getResource().lpush(key, value);
	}
	
	public static void Del(String key) {
		MyServer.jedisPool.getResource().del(key);
	}
}
