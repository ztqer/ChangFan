package Handler;

import java.util.LinkedList;

public class RootHandlerPool extends AbstractHandlerPool {
	public RootHandlerPool(int maxNum) {
		super(maxNum);
		name="Root";
		workingHandlers=new LinkedList<RootHandler>();
		waitingHandlers=new LinkedList<RootHandler>();
	}
	
	@Override
	protected AbstractHandler CreateHandler() {
		return new RootHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}
}
