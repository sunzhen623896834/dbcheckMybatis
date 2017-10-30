package com.inspur.mybatis;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * mybatis配置两个数据源，可以动态传入jdbc连接数据库
 * @author Administrator
 *
 */
 public class MyBatisUtil {
 
	 private static Logger log = LogManager.getLogger(MyBatisUtil.class.getName());//定义日志变量
	 private static final String DBTYPE_DB2 = "DB2";
	 private static final String DBTYPE_ORACLE = "Oracle";
	 private static final String DBTYPE_MYSQL = "MySQL";
	 
     /**
      * 获取默认数据源的SqlSessionFactory
      * @return SqlSessionFactory
      */
     public static SqlSessionFactory getDefaultSqlSessionFactory() {
         String resource = "mybatis-config.xml";
         String environment = "development";  
         InputStream is = MyBatisUtil.class.getClassLoader().getResourceAsStream(resource);
         SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is,environment);
         return factory;
     }
     
     /**
      * 获取默认的数据源，只是更改了名称
      * @return
      */
     public static SqlSessionFactory getSqlSessionFactory() {
         String resource = "mybatis-config.xml";
         String environment = "development";  
         InputStream is = MyBatisUtil.class.getClassLoader().getResourceAsStream(resource);
         SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is,environment);
         return factory;
     }
     
     /**
      * 获取第二数据源的SqlSessionFactory
      * @return SqlSessionFactory
      */
     public static SqlSessionFactory getWorkSqlSessionFactory() {
         String resource = "mybatis-config.xml";
         String environment = "work";  
         InputStream is = MyBatisUtil.class.getClassLoader().getResourceAsStream(resource);
         SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is,environment);
         return factory;
     }
     
     /**
      * 获取默认数据源的SqlSession
      * @return SqlSession
      */
     public static SqlSession getDefaultSqlSession() {
         return getDefaultSqlSessionFactory().openSession();
     }
     
     /**
      * 获取默认数据源的SqlSession
      * @return SqlSession
      */
     public static SqlSession getSqlSession() {
         return getDefaultSqlSessionFactory().openSession();
     }
     
     /**
      * 获取第二数据源的SqlSession
      * @return SqlSession
      */
     public static SqlSession getWorkSqlSession() {
         return getWorkSqlSessionFactory().openSession();
     }
     
     /**
      * 获取默认数据源的SqlSession
      * @param isAutoCommit 
      *         true 表示创建的SqlSession对象在执行完SQL之后会自动提交事务
      *         false 表示创建的SqlSession对象在执行完SQL之后不会自动提交事务，这时就需要我们手动调用sqlSession.commit()提交事务
      * @return SqlSession
      */
     public static SqlSession getDefaultSqlSession(boolean isAutoCommit) {
         return getDefaultSqlSessionFactory().openSession(isAutoCommit);
     }
     
     /**
      * 获取默认数据源的SqlSession
      * @param isAutoCommit 
      *         true 表示创建的SqlSession对象在执行完SQL之后会自动提交事务
      *         false 表示创建的SqlSession对象在执行完SQL之后不会自动提交事务，这时就需要我们手动调用sqlSession.commit()提交事务
      * @return SqlSession
      */
     public static SqlSession getSqlSession(boolean isAutoCommit) {
         return getDefaultSqlSessionFactory().openSession(isAutoCommit);
     }
     
     /**
      * 获取默认数据源的SqlSession
      * @param isAutoCommit 
      *         true 表示创建的SqlSession对象在执行完SQL之后会自动提交事务
      *         false 表示创建的SqlSession对象在执行完SQL之后不会自动提交事务，这时就需要我们手动调用sqlSession.commit()提交事务
      * @return SqlSession
      */
     public static SqlSession getWorkSqlSession(boolean isAutoCommit) {
         return getWorkSqlSessionFactory().openSession(isAutoCommit);
     }
     
     /**
      * 获取数据源的数据库类型
      * @param factoryBeanId
      * @return
     * @throws SQLException 
      */
     public static String getDbType(Connection con) throws SQLException {
 		String dbtype = "";
		if (con != null) {
			try {
				DatabaseMetaData dbmd = con.getMetaData();
				if (dbmd != null) {
					dbtype = dbmd.getDatabaseProductName();
					if (dbtype != null && dbtype.startsWith("DB2/")) {
						dbtype = DBTYPE_DB2;
					} else if (dbtype != null && dbtype.startsWith("Oracle")) {
						dbtype = DBTYPE_ORACLE;
					} else if (dbtype != null && dbtype.startsWith("MySQL")) {
						dbtype = DBTYPE_MYSQL;
					}
				} else {
					log.warn("获取数据库类型出错");
				}
			} catch (SQLException se) {
				log.error("获取数据库类型出错", se);
			} finally {
				con.close();
			}
		}
 		return dbtype;
 	}
     
     /**
      * 动态获取数据库SqlSessionFactory
      * @return
     * @throws IOException 
      */
     public static SqlSessionFactory getSqlSessionFactory
          (String url,String user,String psw,String dbType) throws IOException {
    	 //数据数据库驱动
    	 String driverName = "";
    	 if(dbType.equals("DB2")){
    		 driverName = "com.ibm.db2.jcc.DB2Driver";
    	 }else if(dbType.equals("Oracle")){
    		 driverName = "oracle.jdbc.driver.OracleDriver";
    	 }else if(dbType.equals("MySQL")){
    		 driverName = "com.mysql.jdbc.Driver";
    	 }

    	 //动态配置数据库参数  
    	 Properties properties = new Properties();  
    	 properties.setProperty("jdbc.driver", driverName);  
    	 properties.setProperty("jdbc.url", url);  
    	 properties.setProperty("jdbc.username", user);  
    	 properties.setProperty("jdbc.password", psw);
    	 
    	//加载mybatis配置文件和映射文件  
    	 String resource = "mybatis-config.xml";
    	 String environment = "auto";
    	 Reader reader = Resources.getResourceAsReader(resource);  
    	 SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();  
    	 SqlSessionFactory factory = builder.build(reader,environment, properties);
         return factory;
     }
     
     /**
      * 获取动态数据源的SqlSession
      * @return SqlSession
      */
     public static SqlSession getSqlSession(String url,String user,String psw,String dbType) {
    	 SqlSession sqlSession = null;
    	 try {
			sqlSession = getSqlSessionFactory(url,user,psw,dbType).openSession();
		} catch (IOException e) {
			e.printStackTrace();
		}
         return sqlSession;
     }
     
     /**
      * 获取动态数据源的SqlSession
      * @param isAutoCommit 
      *         true 表示创建的SqlSession对象在执行完SQL之后会自动提交事务
      *         false 表示创建的SqlSession对象在执行完SQL之后不会自动提交事务，这时就需要我们手动调用sqlSession.commit()提交事务
      * @return SqlSession
      */
     public static SqlSession getSqlSession(String url,String user,String psw,String dbType,boolean isAutoCommit) {
    	 SqlSession sqlSession = null;
    	 try {
			sqlSession = getSqlSessionFactory(url,user,psw,dbType).openSession(isAutoCommit);
		} catch (IOException e) {
			e.printStackTrace();
		}
         return sqlSession;
     }
    
 }