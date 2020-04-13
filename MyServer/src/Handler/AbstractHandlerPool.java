package Handler;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import com.mysql.cj.x.protobuf.MysqlxSession.Reset;

public abstract class AbstractHandlerPool {
	public String name;//范型只有父类的静态字段无法实现多态，故把名字在构造时输入
	protected LinkedList workingHandlers;//工作中的Handler
	protected LinkedList waitingHandlers;//等待中的Handler
	private int maxNum;//池大小
	private int count;//池内元素计数
	private final ReentrantLock lock=new ReentrantLock();;//同步锁
	
	//基础构造函数，子类要重写构造函数实例化name和两个LinkedList
	public  AbstractHandlerPool(int maxNum) {
		this.maxNum=maxNum;
	}
	
	//子类获取对应的Handler构造器
	protected abstract AbstractHandler CreateHandler();
	
	//池中重用的对象需重置属性
	protected abstract AbstractHandler Reset(AbstractHandler h);
	
	public IHandler GetHandler(SocketAddress address) {
		//上锁，未到达池上限，创建新Handler并加入工作链表
		lock.lock();
		if(count<maxNum) {
			AbstractHandler h=CreateHandler();
			h.address=address;
			workingHandlers.add(h);
			h.pool=this;
			count++;
			lock.unlock();
			return h;
		}
		//达到池上限，尝试从等待链表取一个
		if(waitingHandlers.size()!=0) {
			AbstractHandler h=(AbstractHandler)waitingHandlers.getFirst();
			h=Reset(h);
			h.address=address;
			lock.unlock();
			return h;
		}
		lock.unlock();
		//等待链表也没有，创建一个新Handler，池对其不作管理
		AbstractHandler h=CreateHandler();
		h.address=address;
		return h;
	}
	
	//上锁，调整元素所属的链表
	public void FinishWork(IHandler h) {
		lock.lock();
		//判断元素归池管理
		if(workingHandlers.remove(h)) {
			waitingHandlers.add(h);
		}
		lock.unlock();
	}
}
