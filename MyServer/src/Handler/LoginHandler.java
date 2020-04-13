package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginHandler extends AbstractHandler {
	private static final ArrayList<String> usernames= new ArrayList<>();
	private static final HashMap<String, String> account=new HashMap<>();
	
	//从ArrayList查询用户名，再从HashMap验证密码
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		String test1="qwer";
        String test2="1234";
        usernames.add("qwer");
        account.put(test1,test2);
        int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        System.out.println(address+"-收到消息："+s1);
    	String s2=usernames.contains(s1)?"验证密码":"不存在此用户";
    	os.write(s2.getBytes());
    	os.flush();
    	if(s2.equals("不存在此用户")) {
    		return;
    	}
        System.out.println(address+"-发出消息："+s2);
        int len3=is.read(buffer);
        String s3=new String(buffer,0,len3);
        System.out.println(address+"-收到消息："+s3);
    	String s4=s3.equals(account.get(test1))?"登录成功":"密码错误";
    	os.write(s4.getBytes());
    	os.flush();
        System.out.println(address+"-发出消息："+s4);
	}
}
