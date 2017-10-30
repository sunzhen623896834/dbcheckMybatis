package com.inspur.dbcheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.inspur.mybatis.MyBatisUtil;
/**
 * 导出insert语句
 * @author sunzhenrj
 *
 */
public class FunctionDownCheck {

	private static Logger log = LogManager.getLogger(FunctionDownCheck.class.getName());//定义日志变量
	public static String url1 = "jdbc:db2://10.110.1.210:50000/v6td";//DB2
	public static String url2 = "jdbc:mysql://10.110.1.177:3306/v6dbgz?useSSL=true";//MySQL
	public static String url3 = "jdbc:oracle:thin:@10.0.8.34:1521:v6db";//Oracle
	public static String user = "db2inst1";
	public static String psw = "db2inst1";
	public static String dbType = "DB2";//DB2 Oracle MySQL
	public static SqlSession sqlSession = MyBatisUtil.getSqlSession(url1, user, psw, dbType);
	
	public static final String outFile = "C:/inspur/sql/";//sql文件生成路径
	
	public static void main(String[] args) throws SQLException, IOException {
		String tabName = "V6PUB_COMMON_HELP";
		String outFileName = outFile+tabName+".sql";
		
		boolean flag = CreateTableSql.deleteFile(outFileName);//删除已经存在的文件，重新生成新文件
		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(new File(outFileName)),"GBK");   
        BufferedWriter writer = new BufferedWriter(write);
        String retStr = getInsertSqlByTabName(tabName);
        writer.write(retStr);
        System.out.println("1111");
	}
	
	/**
	 * 获取insert语句
	 * @param tabName
	 * @return
	 */
	public static String getInsertSqlByTabName(String tabName) {
		String retStr = "";
		String selectSql = getSelectSqlByTabName(tabName);
		List list = sqlSession.selectList("PubSqlTool.commonSelect", selectSql);
		log.debug("list=="+list.size());
		if(list!=null && list.size()>0){
			for(int i=0;i<list.size();i++){
				Map map = (Map) list.get(i);
				String insertSql = (String) map.get("INSERT_SQL") + " \n";
//				log.debug("insertSql=="+insertSql);
				retStr += insertSql;
			}
		}
		return retStr;
	}
	
	/**
	 * 通过表名返回查询语句
	 * @param tabName
	 * @return
	 */
	private static String getSelectSqlByTabName(String tabName) {
		String retSql = "";
		String sql1 = " SELECT 'INSERT INTO "+tabName+" ( ";
		String sql2 = " || ' VALUES ( '";
		String colSql = "SELECT COLNAME,TYPENAME FROM SYSCAT.COLUMNS WHERE TABNAME = '"+tabName+"'";
		List colList = sqlSession.selectList("PubSqlTool.commonSelect", colSql);
		if(colList!=null && colList.size()>0){
			for(int i=0;i<colList.size();i++){
				Map colMap = (Map) colList.get(i);
				String colName = (String) colMap.get("COLNAME");
				sql1 += colName+",";
				sql2 += " || '''' || "+colName+" || '''' || ','";
			}
			if(sql1!=null && sql1.length()>0){
				sql1 = sql1.substring(0, sql1.length()-1);
				sql1 += " )' ";
			}
			if(sql2!=null && sql2.length()>0){
				sql2 = sql2.substring(0, sql2.length()-6);
				sql2 += " || ');' AS INSERT_SQL";
			}
		}
		retSql = "SELECT * FROM ( " + sql1 + sql2 + " FROM " + tabName + " )WHERE INSERT_SQL IS NOT NULL";
		return retSql;
	}
}
