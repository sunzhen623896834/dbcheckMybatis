package com.inspur.dbcheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.inspur.mybatis.MyBatisUtil;

/**
 * 数据结构校验，并生成建议语句。
 * @author Administrator
 *
 */
public class DataStructCheck {
	
	private static final String outFile = "C:/inspur/sql/";//sql文件生成路径
	private static Logger log = LogManager.getLogger(DataStructCheck.class.getName());//定义日志变量	
	

	public static boolean doDataStructCheck(String url,String user,String psw,String dbType,String module) {
		boolean retFlag = false;
		String ip = getIpByUrl(url,dbType);
		boolean mkdirFlag = MakeEmptyDir.doMake();
		String outFileName = outFile+"RESULT_"+user+"_"+dbType+"_"+module+"("+ip+").sql";
		try {
			boolean flag = deleteFile(outFileName);//删除已经存在的文件，重新生成新文件
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName), true));			
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType, true);
			//----开始准备生成create 语句，马上就要开始了
			//------我是华丽丽的分割线 
			/**
			 * 基本思路：
			 * 1、查询出标准的表的数量tableList1
			 * 2、查询出在数据库中存在的表的数量tableList2
			 * 3、剔出tableList1中的tableList2，生成新的tableList
			 * 4、循环tableList，逐个生成create语句
			 */
			List tableList1 = new ArrayList();//V6CHECK_TABLES
			List tableList2 = new ArrayList();//数据库中的表
			List tableList = new ArrayList();//缺少的表
			String selectTable1 = "SELECT TABNAME,TBSPACE,INDEX_TBSPACE "
					+ "FROM V6CHECK_TABLES "
					+ "WHERE TYPE='T' "
					+ "AND TABNAME LIKE '"+module+"%' ORDER BY TABNAME";
			
			String selectTable2DB2 = "SELECT TABNAME,TBSPACE,INDEX_TBSPACE "
					+ "FROM SYSCAT.TABLES "
					+ "WHERE TABSCHEMA = (SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
					+ "AND TYPE='T' AND TABNAME LIKE '"+module+"%' ORDER BY TABNAME ";

			String selectTable2ORACLE = "SELECT A.TABLE_NAME TABNAME,A.TABLESPACE_NAME TBSPACE,B.TABLESPACE_NAME INDEX_TBSPACE "
					+ "FROM USER_TABLES A "
					+"LEFT JOIN USER_INDEXES B ON B.TABLE_NAME = A.TABLE_NAME "
					+ "WHERE A.TABLE_NAME LIKE '"+module+"%' "
					+ "ORDER BY A.TABLE_NAME";
			
			String selectTable2MYSQL = "SELECT UPPER(TABLE_NAME) AS TABNAME,'' AS TBSPACE,'' AS INDEX_TBSPACE "
					+ "FROM INFORMATION_SCHEMA.TABLES "
					+ "WHERE TABLE_SCHEMA=(SELECT DATABASE()) "
					+ "AND TABLE_NAME LIKE '"+module+"%' ORDER BY TABLE_NAME ";
			
			tableList1 = sqlSession.selectList("PubSqlTool.commonSelect", selectTable1);
			if(dbType.equals("DB2")){//DB2
				tableList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectTable2DB2);
			}else if(dbType.equals("Oracle")){//Oracle
				tableList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectTable2ORACLE);
			}else if(dbType.equals("MySQL")){//MySQL
				tableList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectTable2MYSQL);
			}
			
			//循环tableList1、tableList2，找出缺少的表 
			if(tableList1!=null && tableList1.size()>0){
				for(int i=0;i<tableList1.size();i++){
					boolean addFlag = true;
					Map tMap1 = (Map) tableList1.get(i);
					String tabName1 = (String) tMap1.get("TABNAME")==null?"":(String) tMap1.get("TABNAME");
					tabName1 = tabName1.trim().toUpperCase();
					if(tableList2!=null && tableList2.size()>0){
						for(int j=0;j<tableList2.size();j++){
							Map tMap2 = (Map) tableList2.get(j);
							String tabName2 = (String) tMap2.get("TABNAME")==null?"":(String) tMap2.get("TABNAME");
							tabName2 = tabName2.trim().toUpperCase();
							if(tabName1.equals(tabName2)){
								addFlag = false;
								continue;
							}
						}
					}
					if(addFlag==true){
						tableList.add(tMap1);
					}
				}
			}
			
			String retStr = "------此语句是通过程序自动生成的，仅供参考。孙震     \n";//生成建表语句
			retStr += "-----------建表语句开始------- \n";
			if(tableList!=null && tableList.size()>0){
				for(int i=0;i<tableList.size();i++){
					Map tableMap = (Map) tableList.get(i);
					String tableName = (String) tableMap.get("TABNAME");
					tableName = tableName.trim().toUpperCase();
					retStr += AlterOrCreateTable.createSql(tableName,dbType);//根据表名生成整表的建表语句
				}
			}

			//--------生成create语句结束，休息一下，马上回来
			//--------我是华丽丽的分割线 
			retStr +="-----------建表语句结束------- \n";
			
			//生成alter语句开始了，准备好了吗
			//--------我是华丽丽的分割线 
			
			/**
			 * 基本思路：
			 * 1、循环tableList2（数据库中的表），根据tabName查询出标准中的字段的列表colList1
			 * 2、根据不同的数据库类型，依次查询出数据库中tabName的所有字段colList2
			 * 3、轮询colList1、colList2，把colList1中剔出colList2，找出缺少的字段列表colList
			 * 4、根据tabName、colList生成alter语句
			 */
			
			retStr +="-----------alter语句开始------- \n";

			List colList1 = new ArrayList();//V6CHECK_COLUMNS
			List colList2 = new ArrayList();//数据库中的表
			if(tableList2!=null && tableList2.size()>0){//检查数据库中存在的表的字段
				for(int i=0;i<tableList2.size();i++){
					Map tMap3 = (Map) tableList2.get(i);
					String tabName = (String) tMap3.get("TABNAME")==null?"":(String) tMap3.get("TABNAME");
					tabName = tabName.trim().toUpperCase();//防止MySQL大小写的问题，统一把字段名转换为大写字母
					List colList = new ArrayList();//缺少的列
					String selectCol1 = "SELECT TABNAME,COLNAME,TYPENAME,LENGTH,T_SCALE,T_DEFAULT,T_NULLS "
							+ "FROM V6CHECK_COLUMNS WHERE TABNAME = '"+tabName+"' ORDER BY COLNAME";
					String selectCol2DB2 = "SELECT TABNAME,COLNAME,TYPENAME,LENGTH,SCALE T_SCALE,"
							+ "COALESCE(DEFAULT,'') T_DEFAULT,CASE WHEN NULLS='N' THEN 'NO' ELSE 'YES' END T_NULLS "
							+ "FROM SYSCAT.COLUMNS "
							+ "WHERE TABSCHEMA = (SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
							+ "AND TABNAME = '"+tabName+"' ";
					String selectCol2ORACLE = "SELECT TABLE_NAME TABNAME,COLUMN_NAME COLNAME,"
							+ "DATA_TYPE TYPENAME,DATA_LENGTH LENGTH,DATA_SCALE T_SCALE,"
							+ "DATA_DEFAULT T_DEFAULT,"
							+ "CASE WHEN NULLABLE='N' THEN 'NO' ELSE 'YES' END T_NULLS "
							+ "FROM USER_TAB_COLUMNS "
							+ "WHERE TABLE_NAME = '"+tabName+"' ";
					String selectCol2MYSQL = "SELECT UPPER(TABLE_NAME) TABNAME,UPPER(COLUMN_NAME) COLNAME,"
							+ "UPPER(DATA_TYPE) TYPENAME,UPPER(NUMERIC_PRECISION) LENGTH,UPPER(NUMERIC_SCALE) T_SCALE,"
							+ "UPPER(COLUMN_DEFAULT) T_DEFAULT,UPPER(IS_NULLABLE) T_NULLS "
							+ "FROM INFORMATION_SCHEMA.COLUMNS "
							+ "WHERE TABLE_SCHEMA=(SELECT DATABASE()) "
							+ "AND TABLE_NAME = '"+tabName+"' ";
					colList1 = sqlSession.selectList("PubSqlTool.commonSelect", selectCol1);
					if(dbType.equals("DB2")){//DB2
						colList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectCol2DB2);
					}else if(dbType.equals("Oracle")){//Oracle
						colList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectCol2ORACLE);
					}else if(dbType.equals("MySQL")){//MySQL
						colList2 = sqlSession.selectList("PubSqlTool.commonSelect", selectCol2MYSQL);
					}
					for(int j=0;j<colList1.size();j++){
						Map cMap1 = (Map) colList1.get(j);
						boolean addFlag = true;
						String colName1 = (String) cMap1.get("COLNAME")==null?"":(String) cMap1.get("COLNAME");
						colName1 = colName1.trim().toUpperCase();
						for(int m=0;m<colList2.size();m++){
							Map cMap2 = (Map) colList2.get(m);
							String colName2 = (String) cMap2.get("COLNAME")==null?"":(String) cMap2.get("COLNAME");
							colName2 = colName2.trim().toUpperCase();
							if(colName1.equals(colName2)){
								addFlag = false;
								continue;
							}
						}
						if(addFlag==true){
							colList.add(cMap1);
						}
					}

					if(colList!=null && colList.size()>0){
						for(int j=0;j<colList.size();j++){
							Map colMap = (Map) colList.get(j);
							String tableName = (String) colMap.get("TABNAME");
							tableName = tableName.trim().toUpperCase();
							String colName = (String) colMap.get("COLNAME")==null?"":(String) colMap.get("COLNAME");
							colName = colName.trim().toUpperCase();
							retStr += AlterOrCreateTable.alterSql(tableName,colName,dbType);//生成alter语句
						}
					}
				}
			}	
			
			retStr +="-----------alter语句结束------- \n";
			//生成alter语句结束了，是不是还要做点什么呢
			//--------我是华丽丽的分割线 
			

			//查找数据库中的备份表，并生成drop语句
			
			retStr += "-------以下表为数据库中存在的备份表，请确认时候使用，不使用建议删除----- \n";
			retStr += "-----请谨慎操作备份表！！！！！------ \n";
			retStr += "-----删除备份表语句开始------- \n";
			
			String selectTable = "SELECT TABNAME FROM V6CHECK_TABLES WHERE TYPE='T' AND TABNAME LIKE '"+module+"%' ";
			String deleteTableDB2 = "SELECT TABNAME,TBSPACE,INDEX_TBSPACE "
					+ "FROM SYSCAT.TABLES "
					+ "WHERE TABSCHEMA = (SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
					+ "AND TYPE='T' AND TABNAME LIKE '"+module+"%' "
					+ "AND TABNAME NOT IN( "+selectTable+" ) ";

			String deleteTableORACLE = "SELECT A.TABLE_NAME TABNAME,A.TABLESPACE_NAME TBSPACE,B.TABLESPACE_NAME INDEX_TBSPACE "
					+ "FROM USER_TABLES A "
					+"LEFT JOIN USER_INDEXES B ON B.TABLE_NAME = A.TABLE_NAME "
					+ "WHERE A.TABLE_NAME LIKE '"+module+"%' "
					+ "AND A.TABLE_NAME NOT IN( "+selectTable+" ) ";
					
			
			String deleteTableMYSQL = "SELECT UPPER(TABLE_NAME) AS TABNAME,'' AS TBSPACE,'' AS INDEX_TBSPACE "
					+ "FROM INFORMATION_SCHEMA.TABLES "
					+ "WHERE TABLE_SCHEMA=(SELECT DATABASE()) "
					+ "AND TABLE_NAME LIKE '"+module+"%' "
					+ "AND TABLE_NAME NOT IN( "+selectTable+" ) ";
			List deleTableList = new ArrayList();
			if(dbType.equals("DB2")){//DB2
				deleTableList = sqlSession.selectList("PubSqlTool.commonSelect", deleteTableDB2);
			}else if(dbType.equals("Oracle")){//Oracle
				deleTableList = sqlSession.selectList("PubSqlTool.commonSelect", deleteTableORACLE);
			}else if(dbType.equals("MySQL")){//MySQL
				deleTableList = sqlSession.selectList("PubSqlTool.commonSelect", deleteTableMYSQL);
			}
			if(deleTableList!=null && deleTableList.size()>0){
				for(int i=0;i<deleTableList.size();i++){
					Map deleMap = (Map) deleTableList.get(i);
					String tableName = (String) deleMap.get("TABNAME");
					retStr += "DROP TABLE "+tableName.toUpperCase()+" ; \n";
				}
			}
			
			retStr += "-----删除备份表语句结束------- \n";
			
			
			//找出表的表空间与索引表空间与标准中不一致的，并提示出来
			//MySQL不存在表空间的概念，因此不考虑MySQL的情况
			String tabSpaceDB2 = "SELECT A.TABNAME,A.TBSPACE,A.INDEX_TBSPACE,B.TBSPACE TBSPACE1,B.INDEX_TBSPACE INDEX_TBSPACE1 "
					+ "FROM V6CHECK_TABLES A "
					+ "JOIN SYSCAT.TABLES B ON B.TABNAME=A.TABNAME "
					+ "AND B.TABSCHEMA = (SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
					+ "WHERE A.TYPE = 'T' AND A.TABNAME LIKE '"+module+"%' "
					+ "AND A.TBSPACE != B.TBSPACE "
					+ "ORDER BY A.TABNAME ";
			
			String tabSpaceORACLE = "SELECT A.TABNAME,A.TBSPACE,A.INDEX_TBSPACE,B.TABLESPACE_NAME TBSPACE1,C.TABLESPACE_NAME INDEX_TBSPACE1 "
					+ "FROM V6CHECK_TABLES A "
					+ "JOIN USER_TABLES B ON B.TABLE_NAME=A.TABNAME "
					+ "JOIN USER_INDEXES C ON C.TABLE_NAME=A.TABNAME "
					+ "WHERE A.TYPE='T' "
					+ "AND A.TABNAME LIKE '"+module+"%' "
					+ "AND A.TBSPACE != B.TABLESPACE_NAME "
					+ "ORDER BY A.TABNAME ";
			
			List tabSpaceList = new ArrayList();
			if(dbType.equals("DB2")){//DB2
				tabSpaceList = sqlSession.selectList("PubSqlTool.commonSelect", tabSpaceDB2);
			}else if(dbType.equals("Oracle")){//Oracle
				tabSpaceList = sqlSession.selectList("PubSqlTool.commonSelect", tabSpaceORACLE);
			}

			retStr += "-----校验表空间开始------- \n";
			
			if(tabSpaceList!=null &&tabSpaceList.size()>0){
				for(int i=0;i<tabSpaceList.size();i++){
					Map tabMap = (Map) tabSpaceList.get(i);
					String tabName = (String) tabMap.get("TABNAME");//表名称
					String tabSpace = (String) tabMap.get("TBSPACE")==null?"":(String) tabMap.get("TBSPACE");//所在表空间
					String indexSpace = (String) tabMap.get("INDEX_TBSPACE")==null?"":(String) tabMap.get("INDEX_TBSPACE");//所在索引表空间
					String tabSpace1 = (String) tabMap.get("TBSPACE1")==null?"":(String) tabMap.get("TBSPACE1");//数据库中所在表空间
					String indexSpace1 = (String) tabMap.get("INDEX_TBSPACE1")==null?"":(String) tabMap.get("INDEX_TBSPACE1");//数据库中所在索引表空间 
					retStr += "表名称："+tabName+" 表空间："+tabSpace+" 索引表空间："+indexSpace+" 实际表空间："+tabSpace1+" 实际索引表空间："+indexSpace1+"  \n" ;					
				}
			}
			
			retStr += "-----校验表空间结束------- \n";
			
			//校验表的主键
			
			retStr += "-----校验表主键开始------- \n";
			String allTbaleSql = "SELECT TABNAME,TBSPACE,INDEX_TBSPACE FROM V6CHECK_TABLES WHERE TYPE='T' AND TABNAME LIKE '"+module+"%' ";
			List allTableList = new ArrayList();
			if(dbType.equals("DB2")){//DB2
				allTbaleSql += " AND TABNAME IN( "
						+ "SELECT TABNAME "
						+ "FROM SYSCAT.TABLES "
						+ "WHERE TABSCHEMA=(SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
						+ "AND TABNAME LIKE '"+module+"%' ) "
						+ "ORDER BY TABNAME";
				allTableList = sqlSession.selectList("PubSqlTool.commonSelect", allTbaleSql);
			}else if(dbType.equals("Oracle")){//Oracle
				allTbaleSql += " AND TABNAME IN( SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE '"+module+"%' ) ORDER BY TABNAME ";
				allTableList = sqlSession.selectList("PubSqlTool.commonSelect", allTbaleSql);
			}else if(dbType.equals("MySQL")){//MySQL
				allTbaleSql += " AND TABNAME IN( "
						+ "SELECT UPPER(TABLE_NAME) "
						+ "FROM INFORMATION_SCHEMA.TABLES "
						+ "WHERE TABLE_SCHEMA=(SELECT DATABASE()) "
						+ "AND TABLE_NAME LIKE '"+module+"%' ) "
						+ "ORDER BY TABNAME";
				allTableList = sqlSession.selectList("PubSqlTool.commonSelect", allTbaleSql);
			}
			if(allTableList!=null && allTableList.size()>0){
				for(int i=0;i<allTableList.size();i++) {
					Map allTabMap = (Map) allTableList.get(i);
					String tabName = (String) allTabMap.get("TABNAME");
					String sql = "SELECT TABNAME,CONSTNAME,COLNAME FROM V6CHECK_PRIKEYS WHERE TABNAME = '"+tabName+"' ";
					
					String sqlDb2 = "SELECT TABNAME,COLNAME,COLSEQ FROM SYSCAT.KEYCOLUSE WHERE TABNAME = '"+tabName+"' ";
					String sqlOracle = "SELECT A.TABLE_NAME TABNAME,A.COLUMN_NAME COLNAME,A.POSITION COLSEQ "
							+ "FROM USER_CONS_COLUMNS A, USER_CONSTRAINTS B "
							+ "WHERE A.CONSTRAINT_NAME = B.CONSTRAINT_NAME "
							+ "AND B.CONSTRAINT_TYPE = 'P' "
							+ "AND B.TABLE_NAME = '"+tabName+"' ";
					String sqlMySql = "SELECT UPPER(TABLE_NAME) TABNAME,UPPER(COLUMN_NAME) COLNAME,UPPER(ORDINAL_POSITION) COLSEQ "
							+ "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = '"+tabName+"' ";
					
					List table1 = sqlSession.selectList("PubSqlTool.commonSelect", sql);
					List table2 = new ArrayList();
					if(dbType.equals("DB2")){//DB2
						table2 = sqlSession.selectList("PubSqlTool.commonSelect", sqlDb2);
					}else if(dbType.equals("Oracle")){//Oracle
						table2 = sqlSession.selectList("PubSqlTool.commonSelect", sqlOracle);
					}else if(dbType.equals("MySQL")){//MySQL
						table2 = sqlSession.selectList("PubSqlTool.commonSelect", sqlMySql);
					}
					if((table1!=null && table1.size()>0) && (table2!=null &&table2.size()>0)){
						if(table1.size() == table2.size()){
							List table3 = new ArrayList();
							String checkPriSql = " SELECT COLNAME FROM V6CHECK_PRIKEYS WHERE TABNAME='"+tabName+"' ";
							if(dbType.equals("DB2")){//DB2
								String checkSql = sqlDb2+" AND COLNAME IN("+checkPriSql+") ";
								table3 = sqlSession.selectList("PubSqlTool.commonSelect", checkSql);
							}else if(dbType.equals("Oracle")){//Oracle
								String checkSql = sqlOracle+" AND COLUMN_NAME IN("+checkPriSql+") ";
								table3 = sqlSession.selectList("PubSqlTool.commonSelect", checkSql);
							}else if(dbType.equals("MySQL")){//MySQL
								String checkSql = sqlMySql+" AND COLUMN_NAME IN("+checkPriSql+") ";
								table3 = sqlSession.selectList("PubSqlTool.commonSelect", checkSql);
							}
							
							if(table3.size()==table1.size()){
								log.debug("表名称："+tabName+" 的表主键没问题，请继续！");
							}else{
								//以下代码是重复的，暂时不抽取
								//重复代码。。。start
								String priKeyStr = "表名称："+tabName;
								String constNameStr = "";
								String standPriKey = "";
								String actualPriKey = "";
								for(int j=0;j<table1.size();j++){
									Map map1 = (Map) table1.get(j);
									String constName =  (String) map1.get("CONSTNAME");//约束名称
									String colName = (String) map1.get("COLNAME");//字段名称
									constNameStr = constName;
									standPriKey += colName + ",";
								}
								if(standPriKey!=null && standPriKey.length()>0){
									standPriKey = standPriKey.substring(0, standPriKey.length()-1);
								}
								
								for(int k=0;k<table2.size();k++){
									Map map2 = (Map) table2.get(k);
									String colName = (String) map2.get("COLNAME");//字段名称
									actualPriKey += colName + ",";
								}
								if(actualPriKey!=null && actualPriKey.length()>0){
									actualPriKey = actualPriKey.substring(0, actualPriKey.length()-1);
								}
								priKeyStr += " 约束名称："+constNameStr+" 主键："+standPriKey+" 实际主键："+actualPriKey;
								retStr += priKeyStr+" \n";
								
								//重复代码。。。end
								
							}
							
						}else{
							
							//重复代码。。。start
							String priKeyStr = "表名称："+tabName;
							String constNameStr = "";
							String standPriKey = "";
							String actualPriKey = "";
							for(int j=0;j<table1.size();j++){
								Map map1 = (Map) table1.get(j);
								String constName =  (String) map1.get("CONSTNAME");//约束名称
								String colName = (String) map1.get("COLNAME");//字段名称
								constNameStr = constName;
								standPriKey += colName + ",";
							}
							if(standPriKey!=null && standPriKey.length()>0){
								standPriKey = standPriKey.substring(0, standPriKey.length()-1);
							}
							
							for(int k=0;k<table2.size();k++){
								Map map2 = (Map) table2.get(k);
								String colName = (String) map2.get("COLNAME");//字段名称
								actualPriKey += colName + ",";
							}
							if(actualPriKey!=null && actualPriKey.length()>0){
								actualPriKey = actualPriKey.substring(0, actualPriKey.length()-1);
							}
							priKeyStr += " 约束名称："+constNameStr+" 主键："+standPriKey+" 实际主键："+actualPriKey;
							retStr += priKeyStr+" \n";
							
							//重复代码。。。end
						}
					}
					
				}
			}
			
			retStr += "-----校验表主键结束------- \n";
			
			//校验表的字段，找出与数据结构中不一致的，并展示出来
			
			retStr += "-----校验表字段属性开始------- \n";
			
			//带校验表的集合为allTableList
			if(allTableList!=null && allTableList.size()>0){
				for(int m=0;m<allTableList.size();m++) {
					Map allTabMap = (Map) allTableList.get(m);
					String tabName = (String) allTabMap.get("TABNAME");
					String checkColumnSql = "SELECT A.TABNAME,A.COLNAME,A.TYPENAME,CAST(A.LENGTH AS DECIMAL(9,0)) LENGTH,"
							+ "A.T_SCALE,A.T_DEFAULT,A.T_NULLS,"
							+ "B.TYPENAME1,B.LENGTH1,B.T_SCALE1,"
							+ "B.T_DEFAULT1,B.T_NULLS1 "
							+ "FROM V6CHECK_COLUMNS A ";
					
					String selectCol2DB2 = "SELECT TABNAME,COLNAME,TYPENAME TYPENAME1,CHAR(LENGTH) LENGTH1,CHAR(SCALE) T_SCALE1,"
							+ "COALESCE(DEFAULT,'') T_DEFAULT1,CASE WHEN NULLS='N' THEN 'NO' ELSE 'YES' END T_NULLS1 "
							+ "FROM SYSCAT.COLUMNS "
							+ "WHERE TABSCHEMA = (SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1 ) "
							+ "AND TABNAME = '"+tabName+"' ";
					String selectCol2ORACLE = "SELECT TABLE_NAME TABNAME,COLUMN_NAME COLNAME,"
							+ "DATA_TYPE TYPENAME1,DATA_LENGTH LENGTH1,to_char(COALESCE(DATA_SCALE,0)) T_SCALE1,"
							+ "DATA_DEFAULT T_DEFAULT1,"
							+ "CASE WHEN NULLABLE='N' THEN 'NO' ELSE 'YES' END T_NULLS1 "
							+ "FROM USER_TAB_COLUMNS "
							+ "WHERE TABLE_NAME = '"+tabName+"' ";
					String selectCol2MySQL = "SELECT UPPER(TABLE_NAME) TABNAME,UPPER(COLUMN_NAME) COLNAME,"
							+ "UPPER(DATA_TYPE) TYPENAME1,"
							+ "CASE WHEN UPPER(DATA_TYPE)='VARCHAR' OR UPPER(DATA_TYPE)='CHAR' "
							+ "THEN UPPER(CHARACTER_MAXIMUM_LENGTH) "
							+ "ELSE UPPER(NUMERIC_PRECISION) END LENGTH1,"
							+ "CASE WHEN UPPER(DATA_TYPE)='VARCHAR' OR UPPER(DATA_TYPE)='CHAR' "
							+ "THEN 0 ELSE UPPER(NUMERIC_SCALE) END T_SCALE1,"
							+ "UPPER(COLUMN_DEFAULT) T_DEFAULT1,UPPER(IS_NULLABLE) T_NULLS1 "
							+ "FROM INFORMATION_SCHEMA.COLUMNS "
							+ "WHERE TABLE_SCHEMA=(SELECT DATABASE()) "
							+ "AND TABLE_NAME = '"+tabName+"' ";
					List checkColList = new ArrayList(); 
					if(dbType.equals("DB2")){//DB2
						checkColumnSql += " JOIN ( "+selectCol2DB2+" "
								+ ") B ON B.TABNAME = A.TABNAME AND B.COLNAME = A.COLNAME "
								+ "WHERE A.TABNAME = '"+tabName+"' ";
						checkColList = sqlSession.selectList("PubSqlTool.commonSelect", checkColumnSql);
					}else if(dbType.equals("Oracle")){//Oracle
						checkColumnSql += " JOIN ( "+selectCol2ORACLE+" "
								+ ") B ON B.TABNAME = A.TABNAME AND B.COLNAME = A.COLNAME "
								+ "WHERE A.TABNAME = '"+tabName+"' ";
						checkColList = sqlSession.selectList("PubSqlTool.commonSelect", checkColumnSql);
					}else if(dbType.equals("MySQL")){//MySQL
						checkColumnSql += " JOIN ( "+selectCol2MySQL+" "
								+ ") B ON B.TABNAME = A.TABNAME AND B.COLNAME = A.COLNAME "
								+ "WHERE A.TABNAME = '"+tabName+"' ";
						checkColList = sqlSession.selectList("PubSqlTool.commonSelect", checkColumnSql);
					}

					try {
						
						if(checkColList!=null && checkColList.size()>0){
							for(int n=0;n<checkColList.size();n++){
								String compareResult = "";//单个表的校验结果
								Map checkMap = (Map) checkColList.get(n);
								String colName = (String) checkMap.get("COLNAME");//字段名称
								//写入校验表中的信息
								String typeName = (String) checkMap.get("TYPENAME");//字段类型
								typeName = typeName.trim();
								String length = (String) checkMap.get("LENGTH").toString();//字段长度
								length = length.trim();
								String scale = (String) checkMap.get("T_SCALE");//字段长度
								scale = scale.trim();
								String defaultValue = (String) checkMap.get("T_DEFAULT");//字段默认值
								if(defaultValue==null || defaultValue.equals("")){
									defaultValue = "";
								}
								if(typeName.equals("TIMESTAMP")){
									if(dbType.equals("DB2")){
										defaultValue = "CURRENT TIMESTAMP";
									}else if(dbType.equals("Oracle") || dbType.equals("MySQL")){
										defaultValue = "CURRENT_TIMESTAMP";
									}
								}
								defaultValue = defaultValue.trim();
								String isNull = (String) checkMap.get("T_NULLS");//是否可为空
								isNull = isNull.trim();
								
								//数据库中实际的字段信息
								String typeName1 = (String) checkMap.get("TYPENAME1");//字段类型
								typeName1 = typeName1.trim();
								String length1 = (String) checkMap.get("LENGTH1");//字段长度
								length1 = length1.trim();
								String scale1 = (String) checkMap.get("T_SCALE1");;//字段长度	
								scale1 = scale1.trim();
								String defaultValue1 = (String) checkMap.get("T_DEFAULT1");//字段默认值
								String isNull1 = (String) checkMap.get("T_NULLS1");//是否可为空
								isNull1 = isNull1.trim();
								//字段类型根据不同的数据库进行重新整理
								if(dbType.equals("Oracle")){//Oracle
									if(typeName1.equals("NUMBER")){//NUMBER根据不同的情况进行判断,Oracle中把decimal、Integer等统一为number
										if(scale1.equals("0")){
											typeName1 = "INTEGER";
										}else{
											typeName1 = "DECIMAL";
										}
									}else if(typeName1.equals("VARCHAR2")){
										typeName1 = "VARCHAR";
									}else if(typeName1.equals("TIMESTAMP(6)")){
										typeName1 = "TIMESTAMP";
									}
								}else if(dbType.equals("MySQL")){
									if(typeName1.equals("DATETIME")){
										typeName1 = "TIMESTAMP";
									}else if(typeName1.equals("BIGINT")){
										typeName1 = "DECIMAL";
									}else if(typeName1.equals("INT")){
										typeName1 = "INTEGER";
									}else if(typeName1.equals("LONGBLOB")){
										typeName1 = "BLOB";
									}
								}else if(dbType.equals("DB2")){
									if(typeName1.equals("CHARACTER")){
										typeName1 = "CHAR";
									}
								}
								typeName1 = typeName1.trim();
								//默认值重新整理
								if(defaultValue1.contains("0.")){
									defaultValue1 = "0";
								}
								if(defaultValue1==null || defaultValue1.equals("")){
									defaultValue1 = "";
								}
								if(dbType.equals("MySQL")){
									if((typeName1.equals("CHAR") || typeName1.equals("VARCHAR")) && (defaultValue1!=null && !defaultValue1.equals(""))){
										defaultValue1 = "'"+defaultValue1+"'";
									}
								}
								
								defaultValue1 = defaultValue1.trim();
								
								//比较开始
								/**
								 * 1、首先比较类型，如果类型一致，则比较字段的长度
								 * 2、比较默认值
								 * 3、比较是否为空
								 */
								String typeCompare = "";
								String defaultCompare = "";
								String isNullCompare = "";
								if(typeName.equals(typeName1)){
									if(typeName.equals("VARCHAR") || typeName.equals("CHAR")){//VARCHAR、CHAR
										if(!length.equals(length1)){
											typeCompare = " 字段类型："+typeName+" 字段长度："+length+" 实际长度："+length1+" ";
										}
									}else if(typeName.equals("DECIMAL")){//DECIMAL
										//decimal类型的需要比较字段的两个长度，如果都一致才没有问题
										String decimalLenth = length+","+scale;
										String decimalLenth1 = length1+","+scale1;
										if(!decimalLenth.equals(decimalLenth1)){
											typeCompare = " 字段类型："+typeName+" 字段长度："+decimalLenth+" 实际长度："+decimalLenth1+" ";
										}
									}
								}else{
									typeCompare = " 字段类型："+typeName+" 实际字段类型："+typeName1+" ";
								}
								
								if(!defaultValue.equals(defaultValue1)){
									defaultCompare = " 默认值："+defaultValue+" 实际默认值："+defaultValue1+" ";
								}
								
								if(!isNull.equals(isNull1)){
									isNullCompare = " 是否为空："+isNull+" 实际是否为空："+isNull1+" ";
								}
								
								//比较结束
								if((typeCompare!=null && !typeCompare.equals("")) ||
								   (defaultCompare!=null && !defaultCompare.equals("")) ||
								   (isNullCompare!=null && !isNullCompare.equals("")) ){
									compareResult = "表名称："+tabName+" 字段名称："+colName+typeCompare+defaultCompare+isNullCompare+" \n";
								}
								
								if(compareResult!=null && !compareResult.equals("")){
									retStr += compareResult;
								}
								
							}
						}
						
					} catch (Exception e) {
						log.debug("e==="+e.getMessage());
					}
					
				}
			}
			
			retStr += "-----校验表字段属性结束------- \n";
			
			writer.write(retStr);
			writer.write("\n");
			writer.close();
			
			retFlag = true;
		} catch (Exception e) {
			log.debug("e==="+e.getMessage());
		}
		
		return retFlag;
	}
	
	/**
	 * 根据jdbc 的url和数据库类型获取数据库ip
	 * @param url
	 * @param dbType
	 * @return
	 */
	public static String getIpByUrl(String url, String dbType) {

		String ip = "";
		String[] line = null;
		line = url.split(":");
		for(int i=0;i<line.length;i++){
			if(dbType.equals("DB2")){//DB2
				ip = line[2].substring(2, line[2].length());
			}else if(dbType.equals("Oracle")){//Oracle
				ip = line[3].substring(1, line[3].length());
			}else if(dbType.equals("MySQL")){//MySQL
				ip = line[2].substring(2, line[2].length());
			}
		}
		return ip;
	}

	/**
	 * 循环文件夹，找出里面的文件名称
	 * @param strPath
	 * @return
	 */
	public static List<File> getFileList(String strPath) {
		List filelist = new ArrayList();
        File dir = new File(strPath);
        File[] files = dir.listFiles();//该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("XML") || fileName.endsWith("xml")) { // 判断文件名是否以.XML结尾
                    String strFileName = files[i].getName();
                    filelist.add(strFileName);
                } else {
                    continue;
                }
            }
        }
        return filelist;
    }
	
	/**
	 * 删除文件
	 * @param sPath
	 * @return
	 */
	public static boolean deleteFile(String Path) {
		boolean flag = false;
		File file = new File(Path);
		//路径为文件且不为空则进行删除
		if(file.isFile() && file.exists()){
			file.delete();
			flag = true;
		}
		return flag;
	}
	
}