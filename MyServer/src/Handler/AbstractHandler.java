package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public abstract class AbstractHandler implements IHandler {
	protected IHandler next;
	public volatile AbstractHandlerPool pool;
	public volatile SocketAddress address;
	
	//把异常上抛到TcpThread处理
	@Override
	public boolean HandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException{
		try {
			//责任链模式，处理完自己的部分传递下去
			ConcreteHandleMessage(is, os, buffer);
		} catch (IOException e) {
			//连接中断也要保证池更新
			if(pool!=null) {
				pool.FinishWork(this);
			}
			throw e;
		}
		//通知池更新
		if(pool!=null) {
			pool.FinishWork(this);
		}
		if(next!=null) {
			return next.HandleMessage(is, os, buffer);
		}
		//已经是链表尾部，返回完成的信号
		return true;
	};
	
	//子类实现自身的具体逻辑
	protected abstract void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException;
}
