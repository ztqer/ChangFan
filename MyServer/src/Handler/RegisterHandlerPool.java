package Handler;

import java.util.LinkedList;

public class RegisterHandlerPool extends AbstractHandlerPool {
	public RegisterHandlerPool(int maxNum) {
		super(maxNum);
		name="Register";
		workingHandlers=new LinkedList<RegisterHandler>();
		waitingHandlers=new LinkedList<RegisterHandler>();
	}

	@Override
	protected AbstractHandler CreateHandler() {
		return new RegisterHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}
}
