package Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Server.MyServer;
import redis.clients.jedis.Jedis;

public class UpdateHandler extends AbstractHandler {

	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		//检验是否为最新版本
		Jedis jedis=MyServer.jedisPool.getResource();
		String newVersion=jedis.get("version");
		jedis.close();
		int len1=is.read(buffer);
        String oldVersion=new String(buffer,0,len1);
        System.out.println(address+"-收到消息："+oldVersion);
        String s1=newVersion.equals(oldVersion)?"当前为最新版本":"检测到新版本，即将开始下载更新";
        os.write(s1.getBytes());
        os.flush();
        System.out.println(address+"-发出消息："+s1);
        if(s1.equals("当前为最新版本")){
            return;
        }
        is.read();
        //找到相应的补丁包并传输给客户端
        File patchFile=new File("/project/apks/"+oldVersion+"-"+newVersion+".patch");
        FileInputStream fileInputStream=new FileInputStream(patchFile);
        String s2=String.valueOf(patchFile.length());
        os.write(s2.getBytes());
        os.flush();
        System.out.println(address+"-发出消息："+s2);
        is.read();
        int len=-1;
        while((len=fileInputStream.read(buffer))!=-1) {
        	os.write(buffer,0,len);
        	os.flush();
        }
        fileInputStream.close();
        System.out.println(address+"-：文件"+patchFile.getName()+"传输完成");
	}

}
