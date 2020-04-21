package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import Handler.IHandler;
import Handler.HandlerFactory;

public class TcpThread implements Runnable {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private SocketAddress address;
    private byte[] buffer = new byte[1024];//缓冲区大小
    
    //构造时异常上抛给主线程
    public TcpThread(Socket s) throws IOException {
		socket=s;
		is=socket.getInputStream();
		os=socket.getOutputStream();
		address=socket.getRemoteSocketAddress();
		System.out.println("连接成功，客户端IP:"+address);
	}
    
    //线程执行时异常自己处理
	@Override
	public void run(){
		IHandler handler=HandlerFactory.getInstance().GetHandler("Start",address);
		try {
			handler.HandleMessage(is, os, buffer);
		} catch (IOException e1) {
			System.out.println(address+"-连接断开");
		}
		//保证总是能关闭连接
		finally {
			try {
				socket.close();
			} catch (IOException e2) {
				System.out.println(address+"-关闭socket异常");
			}
		}
	}
}
