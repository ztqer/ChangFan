package Handler;

import java.util.LinkedList;

public class BroadcastHandlerPool extends AbstractHandlerPool {
	public BroadcastHandlerPool(int maxNum) {
		super(maxNum);
		name="Broadcast";
		workingHandlers=new LinkedList<BroadcastHandler>();
		waitingHandlers=new LinkedList<BroadcastHandler>();
	}
	
	@Override
	protected AbstractHandler CreateHandler() {
		return new BroadcastHandler();
	}

	//清空消息列表
	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		BroadcastHandler newH=(BroadcastHandler)h;
		newH.message.clear(); 
		return newH;
	}
}
