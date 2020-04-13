package Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;

public class BroadcastHandler extends AbstractHandler{
	//生产者消费者模式，别处生产出需要广播的消息，遍历HashSet,存入缓冲区，线程依次取出消费
	//池外的实例无法释放，但任务本就无限循环(唯一需要处理的情况是连接断开,在上抛异常前释放引用)
	public static final HashSet<BroadcastHandler> receivers=new HashSet<>();
	public volatile LinkedList<String> message=new LinkedList<>();//链表作缓冲区,对其他线程可见故必须声明成volatile
	//构造时把对象加入HashSet
	public BroadcastHandler() {
		receivers.add(this);
	}
	@Override
	protected void ConcreteHandleMessage(InputStream is, OutputStream os, byte[] buffer) throws IOException {
		try {
			while(true) {
				if(!message.isEmpty()) {
					String s=message.pop();
			        System.out.println(address+"-发出消息："+s);
					os.write(s.getBytes());
					os.flush();
				}
			}
		} catch (IOException e) {
			//非池管理对象移出集合使其可被回收
			if(pool==null) {
				receivers.remove(this);
			}
			throw e;
		}
	}
}
