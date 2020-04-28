package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import MyTransacation.RegisterTransacation;
import Server.MyServer;
import redis.clients.jedis.Jedis;

//注册
public class RegisterHandler extends AbstractHandler {
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		Jedis jedis=MyServer.jedisPool.getResource();
		int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        System.out.println(address+"-收到消息："+s1);
        //从redis的SET username 中查询
    	String s2=jedis.sismember("username", s1)?"已存在此用户":"请继续";
    	os.write(s2.getBytes());
    	os.flush();
        System.out.println(address+"-发出消息："+s2);
    	if(s2.equals("已存在此用户")) {
    		return;
    	}
        int len2=is.read(buffer);
        String s3=new String(buffer,0,len2);
        System.out.println(address+"-收到消息："+s3);
        String s4="正在请求管理员分配权限，请稍后尝试登陆";
    	os.write(s4.getBytes());
    	os.flush();
    	RegisterTransacation registerTransacation=RegisterTransacation.CreateRegisterTransacation(s1, s3);
    	if(registerTransacation!=null) {
    		registerTransacation.Start();
    	}
    	jedis.close();
	}
}
