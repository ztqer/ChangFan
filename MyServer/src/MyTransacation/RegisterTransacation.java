package MyTransacation;

import java.util.HashMap;
import java.util.ArrayList;
import Server.RedisWriteUnility;

//注册事务是一个分布式事务，会有隔离性问题：其他用户尝试登陆未注册完成的账号，但根据登陆逻辑自动失败，故不受影响
public class RegisterTransacation implements ITransacation{
	//静态列表和方法来存储和寻找进行中的事务
	private volatile static ArrayList<RegisterTransacation> workingTransacations=new ArrayList<>();
	public static RegisterTransacation GetRegisterTransacation(String username) {
		for(RegisterTransacation t:workingTransacations) {
			if(t.username.equals(username)) {
				return t;
			}
		}
		return null;
	}
	//封装构造函数，防止并发创建同名事务
	//必须通过CreateRegisterTransacation并判断!=null
	public static RegisterTransacation CreateRegisterTransacation(String username,String password) {
		if(GetRegisterTransacation(username)==null) {
			return new RegisterTransacation(username,password);
		}
		return null;
	}
	
	private String username;
	private String password;
	private String permission;
	//state配合数据库锁保证事务同步 0:初始化完成 1:Start完成 2:SetPermission完成  3:Commit完成 100:正在被占用
	private volatile int state;
	
	private RegisterTransacation(String username,String password) {
		this.username=username;
		this.password=password;
		state=0;
	}
	
	//向SET username 添加记录
	@Override
	public void Start() {
		if(state==0) {
			RedisWriteUnility.Lock();
			RedisWriteUnility.Sadd("username", username);
			state=1;
			workingTransacations.add(this);
			RedisWriteUnility.UnLock();
			//事务5分钟未完成,则过期回滚
			Thread thread=new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(300000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally {
						//事务被占用则等待其完成
						while(state==100) {
							continue;
						}
						if(state!=3) {
							Rollback();
						}
					}
				};
			};
			thread.start();
		}
		else {
			Rollback();
		}
	}

	//设置description
	public void SetPermission(String permission) {
		//state置100保证同步（防止一个线程赋值完成但没更改state，别的线程也进入修改）
		if(state==1) {
			state=100;
			this.permission=permission;
			state=2;
		}
		else {
			Rollback();
		}
	}
	
	//向HASH account_具体的username 添加password和permission
	@Override
	public void Commit() {
		if(state==2) {
			RedisWriteUnility.Lock();
			state=100;
			HashMap<String, String> hashMap=new HashMap<>();
			hashMap.put("password", password);
			hashMap.put("permission", permission);
			RedisWriteUnility.Hmset("account_"+username, hashMap);
			state=3;
			workingTransacations.remove(this);
			RedisWriteUnility.UnLock();
			System.out.println(username+"注册成功");
		}
		else {
			Rollback();
		}
	}

	//重置所有数据（无视state）
	@Override
	public void Rollback() {
		//可能多处调用回滚，判断节省重复操作
		if(state==0) {
			return;
		}
		RedisWriteUnility.Lock();
		RedisWriteUnility.Srem("username", username);
		RedisWriteUnility.UnLock();
		permission=null;
		state=0;
		workingTransacations.remove(this);
		System.out.println(username+"注册失败，已回滚");
	}
}
