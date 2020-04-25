package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MyServer {
	//静态资源：线程池、mysql驱动、redis驱动和Handler池
	public static final ThreadPoolExecutor threadPool=new ThreadPoolExecutor(100, 100, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
	public static final MysqlCommunicator sqlCommunicator=new MysqlCommunicator();
	private static JedisPoolConfig config;
	static {
		config=new JedisPoolConfig();
		config.setMaxTotal(1000);
	}
	public static final JedisPool jedisPool=new JedisPool(config);
	
	public static void main(String[] args){
		ServerSocket serverSocket;
        try {
        	serverSocket=new ServerSocket(2414);
			System.out.println("服务器准备就绪"+InetAddress.getLocalHost());
		} catch (Exception e) {
			System.out.println("服务器准备失败");
			e.printStackTrace();
			return;
		}
        //监听连接并包装成线程交付线程池处理
        Socket socket;
		try {
			while((socket = serverSocket.accept() )!= null) {
				//设置read超时20s
				socket.setSoTimeout(20000);
				TcpThread tcpThread=new TcpThread(socket);
				threadPool.execute(tcpThread);
			}
		} catch (Exception e) {
			System.out.println("服务器网络异常");
			e.printStackTrace();
		}
	}
}
