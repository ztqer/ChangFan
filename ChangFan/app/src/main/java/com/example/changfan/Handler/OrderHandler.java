package com.example.changfan.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OrderHandler extends AbstractHandler{
	private String tag;
	private String message;
	//构造接收1.要发出的String信息 2.tag用于BroadcastReceiver判别
	public OrderHandler(String tag,String message){
		this.tag=tag;
		this.message=message;
	}

	@Override
	protected String GetName() {
		return "Order";
	}

	//向服务器发送订单
	@Override
	protected void Work(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		String s3=tag+"/"+message;
		os.write(s3.getBytes());
		os.flush();
	}
}
