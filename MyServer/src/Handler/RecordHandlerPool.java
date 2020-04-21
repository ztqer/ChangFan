package Handler;

import java.util.LinkedList;

public class RecordHandlerPool extends AbstractHandlerPool {
	public RecordHandlerPool(int maxNum) {
		super(maxNum);
		name="Record";
		workingHandlers=new LinkedList<RecordHandler>();
		waitingHandlers=new LinkedList<RecordHandler>();
	}

	@Override
	protected AbstractHandler CreateHandler() {
		return new RecordHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}

}
