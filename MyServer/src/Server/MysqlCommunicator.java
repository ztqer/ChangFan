package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlCommunicator {
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	private static final String DB_URL = "jdbc:mysql://localhost:3306/changfan?useSSL=false&serverTimezone=UTC";
	private static final String user="root";
	private static final String password="zang19980226";
	public MysqlCommunicator() {
		//加载驱动
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
            System.out.println("未找到数据库驱动"); 
			e.printStackTrace();
		}
	}
    public void Communicate(String[] sql) {
		try {
			//连接数据库
			Connection connection = DriverManager.getConnection(DB_URL,user,password);
			//生成statement对象执行语句
			Statement statement=connection.createStatement();
			for(String s:sql) {
				statement.execute(s);
			}
			statement.close();
            connection.close();
        }catch(SQLException e) {
        	System.out.println("数据库连接异常"); 
            e.printStackTrace(); 
        }
	}
}
