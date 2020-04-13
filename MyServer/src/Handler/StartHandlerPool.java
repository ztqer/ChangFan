package Handler;

import java.util.LinkedList;

public class StartHandlerPool extends AbstractHandlerPool{
	public StartHandlerPool(int maxNum) {
		super(maxNum);
		name="Start";
		workingHandlers=new LinkedList<StartHandler>();
		waitingHandlers=new LinkedList<StartHandler>();
	}

	@Override
	protected AbstractHandler CreateHandler() {
		return new StartHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}
}
