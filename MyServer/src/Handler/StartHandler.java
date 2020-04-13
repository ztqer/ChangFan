package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartHandler extends AbstractHandler {
	//连接处理的入口，接受一个字符串，判断前往具体的Handler
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		int len=is.read(buffer);
        String s1=new String(buffer,0,len);
        System.out.println(address+"-收到消息："+s1);
        next=HandlerFactory.getInstance().GetHandler(s1,address);
        String s2=next==null?"拒绝请求":"请继续";
        System.out.println(address+"-发出消息："+s2);
	    os.write(s2.getBytes());
	    os.flush();
	}
}
