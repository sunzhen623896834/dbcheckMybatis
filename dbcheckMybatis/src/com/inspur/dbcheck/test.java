package com.inspur.dbcheck;

import java.sql.SQLException;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.inspur.mybatis.MyBatisUtil;
/**
 * 测试类
 * @author sunzhenrj
 *
 */
public class test {

	private static Logger log = LogManager.getLogger(test.class.getName());//定义日志变量
	public static String url1 = "jdbc:db2://10.110.1.210:50000/v6td";//DB2
	public static String url2 = "jdbc:mysql://10.110.1.177:3306/v6dbgz?useSSL=true";//MySQL
	public static String url3 = "jdbc:oracle:thin:@10.0.8.34:1521:v6db";//Oracle
	public static String user = "db2inst1";
	public static String psw = "db2inst1";
	public static String dbType = "DB2";//DB2 Oracle MySQL
	public static SqlSession sqlSession = MyBatisUtil.getSqlSession(url1, user, psw, dbType);
	public static void main(String[] args) throws SQLException {
		
		
		
	}
}
