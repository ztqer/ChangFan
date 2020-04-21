package Handler;

import java.net.SocketAddress;
import java.util.HashSet;

public class HandlerFactory {
	private static final HashSet<AbstractHandlerPool> handlerPools=new HashSet<>();//用HashSet存储所有池
	private static final int maxNum=10; //设置所有池大小
	//直接静态存储所有池(创建新Handler类必须在此添加并在构造函数里加入HashSet）
	public static final StartHandlerPool startHandlerPool=new StartHandlerPool(maxNum);
	public static final BroadcastHandlerPool broadcastHandlerPool=new BroadcastHandlerPool(maxNum);
	public static final OrderHandlerPool orderHandlerPool=new OrderHandlerPool(maxNum);
	public static final LoginHandlerPool loginHandlerPool=new LoginHandlerPool(maxNum);
	public static final RegisterHandlerPool registerHandlerPool=new RegisterHandlerPool(maxNum);
	public static final RootHandlerPool rootHandlerPool=new RootHandlerPool(maxNum);
	public static final RecordHandlerPool recordHandlerPool=new RecordHandlerPool(maxNum);
	//类按顺序加载，必须放在最后
	private static final HandlerFactory instance=new HandlerFactory();
	
	//单例模式静态工厂
	private HandlerFactory() {
		handlerPools.add(startHandlerPool);
		handlerPools.add(broadcastHandlerPool);
		handlerPools.add(orderHandlerPool);
		handlerPools.add(loginHandlerPool);
		handlerPools.add(registerHandlerPool);
		handlerPools.add(rootHandlerPool);
		handlerPools.add(recordHandlerPool);
	}
	public static HandlerFactory getInstance() {
		return instance;
	}
	
	//通过名字遍历HashSet，调用具体池的GetHandler方法
	public IHandler GetHandler(String s,SocketAddress address) {
		for(AbstractHandlerPool pool:handlerPools) {
			if(s.equals(pool.name)) {
				return pool.GetHandler(address);
			}
		}
		return null;
	}
}
