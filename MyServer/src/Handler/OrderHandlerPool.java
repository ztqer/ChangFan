package Handler;

import java.util.LinkedList;

public class OrderHandlerPool extends AbstractHandlerPool {
	public OrderHandlerPool(int maxNum) {
		super(maxNum);
		name="Order";
		workingHandlers=new LinkedList<OrderHandler>();
		waitingHandlers=new LinkedList<OrderHandler>();
	}
	
	@Override
	protected AbstractHandler CreateHandler() {
		return new OrderHandler();
	}

	@Override
	protected AbstractHandler Reset(AbstractHandler h) {
		return h;
	}

}
