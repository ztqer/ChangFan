package MyTransacation;

//事务
public interface ITransacation {
	void Start();
	void Commit();
	void Rollback();
}
