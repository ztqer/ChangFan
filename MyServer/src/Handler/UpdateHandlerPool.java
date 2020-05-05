package Handler;

import java.util.LinkedList;

public class UpdateHandlerPool extends AbstractHandlerPool {
	public UpdateHandlerPool(int maxNum) {
		super(maxNum);
		name="Update";
		workingHandlers=new LinkedList<UpdateHandler>();
		waitingHandlers=new LinkedList<UpdateHandler>();
	}

	@Override
	protected AbstractHandler CreateHandler() {
		return new UpdateHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}
}
