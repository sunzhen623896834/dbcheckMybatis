package com.inspur.mybatis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SqlTool  
{

	private static Logger log = LogManager.getLogger(SqlTool.class.getName());//定义日志变量
	/**
	 * 执行查询语句 返回 一个LIST （默认数据源）
	 */
	public static List selectAnySqlDefault(String selectSql) {
		List retList = new ArrayList();
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getDefaultSqlSession(true);
			retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的SELECT语句=: " + selectSql);
		}
		return retList;
	}
	
	/**
	 * 执行查询语句 返回 一个LIST （第二数据源）
	 */
	public static List selectAnySqlWork(String selectSql) {
		List retList = new ArrayList();
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getWorkSqlSession(true);
			retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的SELECT语句=: " + selectSql);
		}
		return retList;
	}
	
	/**
	 * 执行查询语句 返回 一个LIST （动态数据源）
	 */
	public static List selectAnySql(String url,String user,String psw,String dbType,String selectSql) {
		List retList = new ArrayList();
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType,true);
			retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的SELECT语句=: " + selectSql);
		}
		return retList;
	}
	
	/**
	 * 根据XXX表的XXX字段取XXX字段（默认数据源）
	 * 
	 * @param xxx 所要查的字段 必须大写
	 * @param table 所对应的表名 必须大写
	 * @param xx 条件名  必须大写
	 * @param value 条件的值
	 * 
	 * 
	 * @return String
	 */
	public static String getXXXfromTableByXXDefault(String xxx, String table,
			String xx, String value) {
		String sql = "select " + xxx + "  from " + table + "  where " + xx
				+ "  = '" + value + "'";
		SqlSession sqlSession = MyBatisUtil.getDefaultSqlSession(true);
		List list = sqlSession.selectList("PubSqlTool.commonSelect",sql);
		String name = null;
		if (list.size() > 0) {
			Map map = (Map) list.get(0);
			name = (String) map.get(xxx);
		} else {
			name = "";
		}
		return name;
	}
	
	/**
	 * 根据XXX表的XXX字段取XXX字段（第二数据源）
	 * 
	 * @param xxx 所要查的字段 必须大写
	 * @param table 所对应的表名 必须大写
	 * @param xx 条件名  必须大写
	 * @param value 条件的值
	 * 
	 * 
	 * @return String
	 */
	public static String getXXXfromTableByXXWork(String xxx, String table,
			String xx, String value) {
		String sql = "select " + xxx + "  from " + table + "  where " + xx
				+ "  = '" + value + "'";
		SqlSession sqlSession = MyBatisUtil.getWorkSqlSession(true);
		List list = sqlSession.selectList("PubSqlTool.commonSelect",sql);
		String name = null;
		if (list.size() > 0) {
			Map map = (Map) list.get(0);
			name = (String) map.get(xxx);
		} else {
			name = "";
		}
		return name;
	}
	
	/**
	 * 根据XXX表的XXX字段取XXX字段（动态数据源）
	 * 
	 * @param xxx 所要查的字段 必须大写
	 * @param table 所对应的表名 必须大写
	 * @param xx 条件名  必须大写
	 * @param value 条件的值
	 * 
	 * 
	 * @return String
	 */
	public static String getXXXfromTableByXX(String url,String user,String psw,String dbType,
			String xxx, String table,String xx, String value) {
		String sql = "select " + xxx + "  from " + table + "  where " + xx
				+ "  = '" + value + "'";
		SqlSession sqlSession = MyBatisUtil.getSqlSession(url,user,psw,dbType,true);
		List list = sqlSession.selectList("PubSqlTool.commonSelect",sql);
		String name = null;
		if (list.size() > 0) {
			Map map = (Map) list.get(0);
			name = (String) map.get(xxx);
		} else {
			name = "";
		}
		return name;
	}
	
	/** 
	 * 执行查询语句 返回 一个String（默认数据源）
	 * 参数：selectSql：SQL语句
	 *       key:要返回的数据在数据库中对应的字段，大写
	 */
	public static String selectAnySqlStringDefault(String selectSql, String key) {
		List retList = null;
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getDefaultSqlSession(true);
				retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
				if (log.isDebugEnabled())
					log.debug("你刚才所执行的UPDATE语句=: " + selectSql);
		}
		String returnString = "";
		if (retList == null || retList.size() == 0) {
			returnString = "";
		} else {
			try {
				returnString = (String) ((Map) retList.get(0)).get(key);
			} catch (Exception e) {
				throw new RuntimeException("无法获得相应的数据！请检查！");
			}
		}
		return returnString;
	}
	
	/** 
	 * 执行查询语句 返回 一个String（第二数据源）
	 * 参数：selectSql：SQL语句
	 *       key:要返回的数据在数据库中对应的字段，大写
	 */
	public static String selectAnySqlStringWork(String selectSql, String key) {
		List retList = null;
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getWorkSqlSession(true);
				retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
				if (log.isDebugEnabled())
					log.debug("你刚才所执行的UPDATE语句=: " + selectSql);
		}
		String returnString = "";
		if (retList == null || retList.size() == 0) {
			returnString = "";
		} else {
			try {
				returnString = (String) ((Map) retList.get(0)).get(key);
			} catch (Exception e) {
				throw new RuntimeException("无法获得相应的数据！请检查！");
			}
		}
		return returnString;
	}
	
	/** 
	 * 执行查询语句 返回 一个String（动态数据源）
	 * 参数：selectSql：SQL语句
	 *       key:要返回的数据在数据库中对应的字段，大写
	 */
	public static String selectAnySqlString(String url,String user,String psw,String dbType,
			String selectSql, String key) {
		List retList = null;
		if (selectSql != null && !"".equals(selectSql)) {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType,true);
				retList = sqlSession.selectList("PubSqlTool.commonSelect",selectSql);
				if (log.isDebugEnabled())
					log.debug("你刚才所执行的UPDATE语句=: " + selectSql);
		}
		String returnString = "";
		if (retList == null || retList.size() == 0) {
			returnString = "";
		} else {
			try {
				returnString = (String) ((Map) retList.get(0)).get(key);
			} catch (Exception e) {
				throw new RuntimeException("无法获得相应的数据！请检查！");
			}
		}
		return returnString;
	}
	
	/**
	 * 执行更新语句 返回 影响的结果行 （默认数据源）
	 */
	public static int updateAnySqlDefault(String updateSql) {
		int r = 0;
		if (updateSql != null && !"".equals(updateSql)) {
			SqlSession sqlSession = MyBatisUtil.getDefaultSqlSession(true);
			r = sqlSession.update("PubSqlTool.commonUpdate",updateSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的UPDATE语句=: " + updateSql);
		}
		return r;
	}
	
	/**
	 * 执行更新语句 返回 影响的结果行 （第二数据源）
	 */
	public static int updateAnySqlWork(String updateSql) {
		int r = 0;
		if (updateSql != null && !"".equals(updateSql)) {
			SqlSession sqlSession = MyBatisUtil.getWorkSqlSession(true);
			r = sqlSession.update("PubSqlTool.commonUpdate",updateSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的UPDATE语句=: " + updateSql);
		}
		return r;
	}
	
	/**
	 * 执行更新语句 返回 影响的结果行 （动态数据源）
	 */
	public static int updateAnySql(String url,String user,String psw,String dbType,String updateSql) {
		int r = 0;
		if (updateSql != null && !"".equals(updateSql)) {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType, true);
			r = sqlSession.update("PubSqlTool.commonUpdate",updateSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的UPDATE语句=: " + updateSql);
		}
		return r;
	}

	/**
	 * 执行插入语句 返回 影响的结果行 （默认数据源）
	 * @param String insertSql
	 * @author 
	 */
	public static int insertAnySqlDefault(String insertSql) {
		int r = 0;
		if (insertSql != null && !"".equals(insertSql)) {
			SqlSession sqlSession = MyBatisUtil.getDefaultSqlSession(true);
			r = sqlSession.insert("PubSqlTool.commonInsert",insertSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的INSERT语句=: " + insertSql);
		}
		return r;
	}
	
	/**
	 * 执行插入语句 返回 影响的结果行 （第二数据源）
	 * @param String insertSql
	 * @author 
	 */
	public static int insertAnySqlWork(String insertSql) {
		int r = 0;
		if (insertSql != null && !"".equals(insertSql)) {
			SqlSession sqlSession = MyBatisUtil.getWorkSqlSession(true);
			r = sqlSession.insert("PubSqlTool.commonInsert",insertSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的INSERT语句=: " + insertSql);
		}
		return r;
	}
	
	/**
	 * 执行插入语句 返回 影响的结果行 （第二数据源）
	 * @param String insertSql
	 * @author 
	 */
	public static int insertAnySql(String url,String user,String psw,String dbType,String insertSql) {
		int r = 0;
		if (insertSql != null && !"".equals(insertSql)) {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType, true);
			r = sqlSession.insert("PubSqlTool.commonInsert",insertSql);
			if (log.isDebugEnabled())
				log.debug("你刚才所执行的INSERT语句=: " + insertSql);
		}
		return r;
	}
	
	
}