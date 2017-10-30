package com.inspur.dbcheck;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ibatis.session.SqlSession;

import com.inspur.mybatis.MyBatisUtil;
import com.inspur.pub.DateTool;
import com.inspur.pub.Dom4ParseXML;

/**
 * 生成校验表的数据
 * @author Administrator
 *
 */
public class WriteCheckTable {

	private static Logger log = LogManager.getLogger(WriteCheckTable.class.getName());//定义日志变量
	public static final String inFile = "C:/inspur/xml/";//xml文件存放路径
	
	/**
	 * 根据数据库信息生成校验表的数据
	 * @param url
	 * @param user
	 * @param psw
	 * @param dbType
	 * @return
	 */
	public static boolean doWriteCheckTable(String url,String user,String psw,String dbType,String module) {
		boolean retFlag = false;
		
		boolean mkdirFlag = MakeEmptyDir.doMake();		
		boolean flag = checkTable(url,user,psw,dbType,module);//检查表是否存在，如果不存在则创建
		String fileName = "";
		String currentTime = DateTool.getCurrentTimeMillisAsString();
		List fileList = getFileList(inFile);
		try {
			if(fileList!=null && fileList.size()>0){
				for(int f=0;f<fileList.size();f++){
					String name = (String) fileList.get(f);//文件夹下的文件名称
					fileName = inFile + name;//生成文件名称
					String inxml = Dom4ParseXML.parseXML(fileName);
					List moduleList = Dom4ParseXML.getElementList(inxml,"app-data/database/subsystem", "module",
							Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
					log.debug("取到的moduleList size==" + moduleList.size());
					for (int i=0; i<moduleList.size();i++) {// 模块
						Map moduleMap = (Map) moduleList.get(i);
						List subModuleList = Dom4ParseXML.getElementList((String) moduleMap.get("asXML"), 
								"module", "submodule",Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
						for (int j=0;j<subModuleList.size();j++) {// 子模块
							Map subModuleMap = (Map) subModuleList.get(j);
							List tableList = Dom4ParseXML.getElementList((String) subModuleMap.get("asXML"), "submodule",
									"table", Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
							for (int t=0;t<tableList.size();t++) {// 表
								Map tableMap = (Map) tableList.get(t);
								String tblId = (String) tableMap.get("ID");//表名，英文
								tblId = tblId.trim();
								String tblName = (String) tableMap.get("NAME");//表的中文描述
								if(tblId.contains(module) && !tblId.endsWith("_L") && !tblId.endsWith("_V") ){
									boolean writeFlag = insertTable(url,user,psw,dbType,tableMap,currentTime);
									log.debug("写入校验表，待校验的表为："+tblName+":"+tblId+",结果是:"+writeFlag);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.debug("doWriteCheckTable==e=="+e.getMessage());
		}
		
		retFlag = true;
		log.debug("doWriteCheckTable==="+retFlag);
		return retFlag;
	}
	
	/**
	 * 循环xml，把相关信息写入check表
	 * @param currentTime 
	 * @param tblId
	 * @return
	 */
	public static boolean insertTable(String url,String user,String psw,String dbType,Map tableMap, String currentTime) {

		SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType, true);
		List columnList = Dom4ParseXML.getElementList((String) tableMap.get("asXML"), "table", "column",Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
		String tblId = (String) tableMap.get("ID");//英文表名称
		String tblName = (String) tableMap.get("NAME");//中文标描述
		String tblSpace = (String) (tableMap.get("TSP")==null?"":tableMap.get("TSP"));//表空间
		String tblIndexSpace = (String) (tableMap.get("IDXTSP")==null?"":tableMap.get("IDXTSP"));//索引表空间
		StringBuffer deleTblBuffer = new StringBuffer();//删除V6CHECK_TABLES中关于该表的信息
		StringBuffer delePriBuffer = new StringBuffer();//删除V6CHECK_PRIKEYS中关于该表的信息
		StringBuffer deleColBuffer = new StringBuffer();//删除V6CHECK_COLUMNS中关于该表的信息
		StringBuffer insertTblBuffer = new StringBuffer();//插入V6CHECK_TABLES中关于该表的信息
		StringBuffer insertPriBuffer = new StringBuffer();//插入V6CHECK_PRIKEYS中关于该表的信息
		StringBuffer insertColBuffer = new StringBuffer();//插入V6CHECK_COLUMNS中关于该表的信息
		//V6CHECK_TABLES
		if(columnList!=null && columnList.size()>0){
			deleTblBuffer.append("DELETE FROM V6CHECK_TABLES WHERE TABNAME='"+tblId.toUpperCase()+"'");
			int delTableNum = sqlSession.update("PubSqlTool.commonUpdate", deleTblBuffer.toString());
			log.debug("V6CHECK_TABLES中删除表："+tblId+"成功，删除记录条数："+delTableNum);
			insertTblBuffer.append("INSERT INTO V6CHECK_TABLES (TABSCHEMA,TABNAME,TYPE,TBSPACE,INDEX_TBSPACE,UPDATE_TIME) \n");
			insertTblBuffer.append(" VALUES( \n");
			insertTblBuffer.append(" 'DB2INST1', '"+tblId.toUpperCase()+"', 'T' , '"+tblSpace+"', '"+tblIndexSpace+"', '"+currentTime+"' ");
			insertTblBuffer.append(" ) \n");
			int insertNum = sqlSession.update("PubSqlTool.commonUpdate", insertTblBuffer.toString());
		}
		//V6CHECK_COLUMNS
		deleColBuffer.append("DELETE FROM V6CHECK_COLUMNS WHERE TABNAME='"+tblId.toUpperCase()+"'");
		int delColNum = sqlSession.update("PubSqlTool.commonUpdate", deleColBuffer.toString());
		log.debug("删除V6CHECK_COLUMNS中关于表："+tblId+"的信息成功，删除条数为："+delColNum);
		for (int c=0;c<columnList.size();c++) {
			Map colMap = (Map) columnList.get(c);
			String colId = (String) (colMap.get("ID")==null?"":colMap.get("ID"));
			String colType = (String) (colMap.get("TYPE")==null?"":colMap.get("TYPE"));
			colType = colType.trim().toUpperCase();
			if(colType.equals("NUMERIC") || colType.contains("DECIMAL") || colType.contains("NUMERIC")){
				colType = "DECIMAL";
			}else if(colType.equals("CHARACTER")){
				colType = "CHAR";
			}
			String colSize = (String) (colMap.get("SIZE")==null?"":colMap.get("SIZE"));
			colSize = colSize.trim().toUpperCase();
			if(colSize.equals("")){
				colSize = "1";
			}
			String size1 = "0";
			String size2 = "0";
			if(colType.equals("INTEGER")){
				size1 = "4";
				size2 = "0";
			}else if(colType.equals("TIMESTAMP")){
				size1 = "10";
				size2 = "6";
			}else if(colType.equals("VARCHAR") || colType.equals("CHAR")){
				size1 = colSize;
				size2 = "0";
			}else{
				size1 = "30";
				size2 = "0";
			}
			if(colSize.contains(",")){
				int num = colSize.indexOf(",");
				size1 = colSize.substring(0,num);
				size2 = colSize.substring(num+1,colSize.length());
			}
			String colDefault = (String) (colMap.get("DEFAULT")==null?"":colMap.get("DEFAULT"));
			colDefault = colDefault.trim();
			if(colType.equals("VARCHAR") && (colDefault!=null && !colDefault.equals(""))){
				colDefault = "''"+colDefault+"''";
			}
			String ifNull = "YES";
			String colRequired = (String) (colMap.get("REQUIRED")==null?"":colMap.get("REQUIRED"));
			colRequired = colRequired.trim().toUpperCase();
			if(colRequired.equals("TRUE")){
				ifNull = "NO";
			}			
			insertColBuffer.append("INSERT INTO V6CHECK_COLUMNS(TABSCHEMA,TABNAME,COLNAME,TYPENAME,"
					+ "LENGTH,T_SCALE,T_DEFAULT,T_NULLS,UPDATE_TIME) \n");
			insertColBuffer.append("VALUES( \n");
			insertColBuffer.append(" 'DB2INST1', '"+tblId.toUpperCase()+"', '"+colId.toUpperCase()+"', '"+colType.toUpperCase()+"', \n");
			insertColBuffer.append("'"+size1+"', '"+size2+"', '"+colDefault+"', '"+ifNull+"', '"+currentTime+"' \n");
			insertColBuffer.append(") \n");
			log.debug("insertColBuffer=="+insertColBuffer);
			sqlSession.update("PubSqlTool.commonUpdate", insertColBuffer.toString());
			insertColBuffer = new StringBuffer();
		}
		//V6CHECK_PRIKEYS
		delePriBuffer.append("DELETE FROM V6CHECK_PRIKEYS WHERE TABNAME='"+tblId.toUpperCase()+"'");
		int delPriNum = sqlSession.update("PubSqlTool.commonUpdate", delePriBuffer.toString());
		log.debug("删除V6CHECK_PRIKEYS中关于表："+tblId+"的信息成功，删除条数为："+delPriNum);
		for (int c=0;c<columnList.size();c++) {
			Map colMap = (Map) columnList.get(c);
			String colId = (String) colMap.get("ID");
			String colPri = (String) (colMap.get("PRIMARYKEY") == null?"":colMap.get("PRIMARYKEY"));
			colPri = colPri.trim().toUpperCase();
			if ("TRUE".equals(colPri)) {
				insertPriBuffer.append("INSERT INTO V6CHECK_PRIKEYS(TABSCHEMA,TABNAME,CONSTNAME,COLNAME,UPDATE_TIME) \n");
				insertPriBuffer.append("VALUES ( \n");
				insertPriBuffer.append("'DB2INST1', '"+tblId.toUpperCase()+"', '"+tblId.toUpperCase()+"_PK', '"+colId.toUpperCase()+"', '"+currentTime+"' \n");
				insertPriBuffer.append(" ) \n");
				sqlSession.update("PubSqlTool.commonUpdate", insertPriBuffer.toString());
				insertPriBuffer = new StringBuffer();
			}
		}
		return false;
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
	 * 检查表是否存在，如果不存在则创建,并清空表中的数据
	 * @return
	 */
	public static boolean checkTable(String url,String user,String psw,String dbType,String module) {
		boolean flag = false;
		StringBuffer tableSql = new StringBuffer();
        StringBuffer columnSql = new StringBuffer();
        StringBuffer prikeySql = new StringBuffer(); 
        SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, psw, dbType, true);
        tableSql.append("CREATE TABLE V6CHECK_TABLES ( \n");
        tableSql.append(" TABSCHEMA VARCHAR(30) NOT NULL, \n");
        tableSql.append(" TABNAME VARCHAR(100) NOT NULL, \n");
        tableSql.append(" TYPE CHAR(1), \n");
        tableSql.append(" TBSPACE VARCHAR(30), \n");
        tableSql.append(" INDEX_TBSPACE VARCHAR(30), \n");
        tableSql.append(" UPDATE_TIME   VARCHAR(14), \n");
        tableSql.append(" CONSTRAINT V6CHECK_TABLES_PK PRIMARY KEY(TABSCHEMA,TABNAME) \n");
        tableSql.append(") \n");
        
        columnSql.append("CREATE TABLE V6CHECK_COLUMNS ( \n");
        columnSql.append(" TABSCHEMA VARCHAR(30) NOT NULL, \n");
        columnSql.append(" TABNAME VARCHAR(100) NOT NULL, \n");
        columnSql.append(" COLNAME VARCHAR(50) NOT NULL, \n");
        columnSql.append(" TYPENAME VARCHAR(100), \n");
        columnSql.append(" LENGTH INTEGER NOT NULL, \n");
        columnSql.append(" T_SCALE VARCHAR(30), \n");
        columnSql.append(" T_DEFAULT VARCHAR(30), \n");
        columnSql.append(" T_NULLS VARCHAR(30), \n");
        columnSql.append(" UPDATE_TIME VARCHAR(14), \n");
        columnSql.append(" CONSTRAINT V6CHECK_COLUMNS_PK PRIMARY KEY(TABSCHEMA,TABNAME,COLNAME) \n");
        columnSql.append(") \n");
        
        prikeySql.append("CREATE TABLE V6CHECK_PRIKEYS ( \n");
        prikeySql.append(" TABSCHEMA VARCHAR(30) NOT NULL, \n");
        prikeySql.append(" TABNAME VARCHAR(100) NOT NULL, \n");
        prikeySql.append(" CONSTNAME VARCHAR(150) NOT NULL, \n");
        prikeySql.append(" COLNAME VARCHAR(50) NOT NULL, \n");
        prikeySql.append(" UPDATE_TIME VARCHAR(14), \n");
        prikeySql.append(" CONSTRAINT V6CHECK_PRIKEYS_PK PRIMARY KEY(TABSCHEMA,TABNAME,CONSTNAME,COLNAME ) \n");
        prikeySql.append(") \n");
        //清空表中存在的记录
        String deleTableSql = "DELETE FROM V6CHECK_TABLES WHERE TABNAME LIKE '"+module+"%' ";
        String deleColumSql = "DELETE FROM V6CHECK_COLUMNS WHERE TABNAME LIKE '"+module+"%' ";
        String delePrikeySql = "DELETE FROM V6CHECK_PRIKEYS WHERE TABNAME LIKE '"+module+"%' ";
        Connection conn = null;
        int count = 0; 
        try {
        	sqlSession.update("PubSqlTool.commonUpdate", tableSql.toString());
	        log.debug("创建表  V6CHECK_TABLES 成功！");
	        count = 1;
		} catch (Exception e) {
			log.debug("表  V6CHECK_TABLES 已存在！");		
		}
        try {
        	sqlSession.update("PubSqlTool.commonUpdate", columnSql.toString());
			log.debug("创建表  V6CHECK_COLUMNS 成功！");
			count = 2;
		} catch (Exception e) {
			log.debug("表  V6CHECK_COLUMNS 已存在！");
		}
        try {
        	sqlSession.update("PubSqlTool.commonUpdate", prikeySql.toString());
			log.debug("创建表  V6CHECK_PRIKEYS 成功！");
			count = 3;
		} catch (Exception e) {
			log.debug("表  V6CHECK_PRIKEYS 已存在！");
		}
        
		try {
			int deleNum1 = sqlSession.update("PubSqlTool.commonUpdate", deleTableSql);
			log.debug("删除V6CHECK_TABLES表中的记录为："+deleNum1);
		} catch (Exception e) {
			log.debug("e=V6CHECK_TABLES="+e.getMessage());
		}
		
		try {
			int deleNum2 = sqlSession.update("PubSqlTool.commonUpdate", deleColumSql);
			log.debug("删除V6CHECK_COLUMNS表中的记录为："+deleNum2);
		} catch (Exception e) {
			log.debug("e=V6CHECK_COLUMNS="+e.getMessage());
		}
        
		try {
			int deleNum3 = sqlSession.update("PubSqlTool.commonUpdate", delePrikeySql);
			log.debug("删除V6CHECK_PRIKEYS表中的记录为："+deleNum3);
		} catch (Exception e) {
			log.debug("e=V6CHECK_PRIKEYS="+e.getMessage());
		}
		if(count>1){
			flag = true;
		}
		log.debug("检查表是否存在："+flag);
		return flag;
	}
}
