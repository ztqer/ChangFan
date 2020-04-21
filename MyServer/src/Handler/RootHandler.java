package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import MyTransacation.RegisterTransacation;

//管理员设置权限，完成注册
public class RootHandler extends AbstractHandler {
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        int len1=is.read(buffer);
        String s1=new String(buffer,0,len1);
        System.out.println(address+"-收到消息："+s1);
        String s2="继续";
        os.write((s2).getBytes());
        os.flush();
        System.out.println(address+"-发出消息："+s1);
        int len3=is.read(buffer);
        String s3=new String(buffer,0,len3);
        System.out.println(address+"-收到消息："+s3);
        RegisterTransacation transacation=RegisterTransacation.GetRegisterTransacation(s1);
        if(transacation!=null) {
        	transacation.SetPermission(s3);
        	transacation.Commit();
        }
	}
}
