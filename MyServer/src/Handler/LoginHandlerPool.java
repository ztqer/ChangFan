package Handler;

import java.util.LinkedList;

public class LoginHandlerPool extends AbstractHandlerPool {
	public LoginHandlerPool(int maxNum) {
		super(maxNum);
		name="Login";
		workingHandlers=new LinkedList<LoginHandler>();
		waitingHandlers=new LinkedList<LoginHandler>();
	}
	
	@Override
	protected AbstractHandler CreateHandler() {
		return new LoginHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}

}
