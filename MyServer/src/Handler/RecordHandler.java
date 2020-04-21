package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import MyTransacation.RecordTransacation;

public class RecordHandler extends AbstractHandler {
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		int len=is.read(buffer);
        String s1=new String(buffer,0,len);
        System.out.println(address+"-收到消息："+s1);
        RecordTransacation recordTransacation=RecordTransacation.CreateRecordTransacation(s1);
        if(recordTransacation!=null) {
        	recordTransacation.StartAndCommit();
        }
        String s2=recordTransacation.GetResult();
        os.write(s2.getBytes());
        os.flush();
        System.out.println(address+"-发出消息："+s2);
	}
}
