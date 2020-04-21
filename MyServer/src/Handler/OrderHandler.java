package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OrderHandler extends AbstractHandler {
	//把接受的消息通知所有BroadcastHandelr
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		int len=is.read(buffer);
        String s=new String(buffer,0,len);
        System.out.println(address+"-收到消息："+s);
        if(!BroadcastHandler.receivers.isEmpty()) {
            for(BroadcastHandler h:BroadcastHandler.receivers) {
            	h.message.add(s);
            }
        }
	}
}
