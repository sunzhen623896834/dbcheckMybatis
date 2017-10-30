package com.inspur.pub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;


public class DbConnection {
	
	static String url1 = "jdbc:db2://10.110.1.210:50000/v6td";//db2
	static String url2 = "jdbc:mysql://10.110.1.210:3306/v6db";//MySQL
	static String url3 = "jdbc:oracle:thin:@10.0.8.34:1521:v6db";//Oracle
	private static Log log = LogFactory.getLog(DbConnection.class);

	/**
	 * 根据数据库类型获取数据库连接
	 * @param url
	 * @param user
	 * @param psw
	 * @param dbType
	 * @return
	 */
	public static Connection getConnection(String url,String user,String psw,String dbType) {
		Connection conn = null;
		if(dbType.equals("DB2")){
        	conn = DbConnection.getDb2Connection(url, user, psw);
        }else if(dbType.equals("Oracle")){
        	conn = DbConnection.getOracleConnection(url, user, psw);
        }else if(dbType.equals("MySQL")){
        	conn = DbConnection.getMysqlConnection(url, user, psw);
        }
		return conn;
	}
	
	/**
	 * 执行update语句
	 * @param url
	 * @param user
	 * @param psw
	 * @param dbType
	 * @param updateSql
	 * @return
	 */
	public static int executeUpdate(Connection conn,String updateSql) {
		int retNum = 0;
		try {
			Statement sta = conn.createStatement();
			log.debug("执行的sql为："+updateSql.toString());
			retNum = sta.executeUpdate(updateSql.toString());
			sta.close();
			conn.close();
		} catch (Exception e) {
			log.debug("executeUpdate=e=="+e.getMessage());
		}
		return retNum;
	}
		
	/**
	 * 连接DB2数据库
	 * @return
	 */
	public static Connection getDb2Connection(String url,String user,String psw) {
		Connection conn = null;
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			conn = DriverManager.getConnection(url, user, psw);
			conn.setAutoCommit(true);
			System.out.println("连接DB2数据库成功,ok!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	/**
	 * 获取数据库连接是否正常
	 * @param url
	 * @param user
	 * @param psw
	 * @param dbType
	 * @return
	 */
	public static boolean testDbConnectionByUrl(String url,String user,String psw,String dbType){
		Connection conn = null;
		boolean flag = false;
		try {
			if(dbType.equals("DB2")){
				conn = getDb2Connection(url,user,psw);
				String sql = "SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1";
				List list = getResultToList(conn, sql);
				if(list!=null && list.size()>0){
					flag = true;
				}
			}else if(dbType.equals("Oracle")){
				conn = getOracleConnection(url,user,psw);
				String sql = "SELECT DUMMY FROM DUAL";
				List list = getResultToList(conn, sql);
				if(list!=null && list.size()>0){
					flag = true;
				}
			}else if(dbType.equals("MySQL")){
				conn = getMysqlConnection(url,user,psw);
				String sql = "SELECT DATABASE()";
				List list = getResultToList(conn, sql);
				if(list!=null && list.size()>0){
					flag = true;
				}
			}
		} catch (Exception e) {
			log.debug("testDbConnectionByUrl=e=="+e.getMessage());
		}
		
		return flag;
	}
	
	/**
	 * 执行查询sql，并组织结果集为list
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public static List<Map<String, Object>> getResultToList(Connection conn,String sql) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		log.debug("执行的sql为："+sql);
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		if (null == sql || "".equals(sql)) {
			return new ArrayList<Map<String, Object>>();
		}
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if (null != rs) {
				ResultSetMetaData rsm = rs.getMetaData();
				int count = rsm.getColumnCount();
				Map<String, Object> record = null;
				if (count > 0) {
					while (rs.next()) {
						record = new HashMap<String, Object>();
						for (int j = 0; j < count; j++) {
							Object obj = rs.getObject(j + 1);
							String columnName = rsm.getColumnName(j + 1);
							record.put(columnName.toUpperCase(),(obj == null)?"":obj);
						}
						retList.add(record);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ps.close();
			rs.close();
			DbConnection.closeConn(conn);
		}
		return retList;
	}  
	
	
	/**
	 * 连接MYSQL数据库
	 * @return
	 */
	public static Connection getMysqlConnection(String url,String user,String psw) {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, psw);
			conn.setAutoCommit(true);
			System.out.println("连接MySQL数据库成功,ok!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 连接Oracle数据库
	 * @return
	 */
	public static Connection getOracleConnection(String url,String user,String psw) {
		Connection conn = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(url, user, psw);
			conn.setAutoCommit(true);
			System.out.println("连接Oracle数据库成功,ok!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接
	 * @param con
	 */
	public static void closeConn(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
