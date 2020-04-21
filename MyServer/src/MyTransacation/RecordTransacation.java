package MyTransacation;

public abstract class RecordTransacation implements ITransacation {
	protected String result;
	
	//判别类型交由不同的子类实现事务
	public static RecordTransacation CreateRecordTransacation(String data) {
		String type=null;
		String newData=null;
		for(int i=0;i<=data.length()-2;i++) {
			if(data.charAt(i)=='/') {
				type=data.substring(0,i);
				newData=data.substring(i+1);
				break;
			}
		}
		switch (type) {
		    case "Order":
		    	return new OrderRecordTransacation(newData);
		    case "ClothKind":
	    		return new ClothKindRecordTransacation(newData);
		    case "Inventory":
				return new InventoryRecordTransacation(newData);
			case "Update":
				return new UpdateRecordTransacation(newData);
		}
		return null;
	}
	
	//外界调用此方法一步完成
	public void StartAndCommit() {
		Start();
		Commit();
	}
	
	//由子类逻辑赋值result，RecordHandler调用GetResult向客户端返回结果
	public String GetResult() {
		return result;
	};
}
