package com.inspur.dbcheck;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.inspur.pub.Dom4ParseXML;

/**
 * 根据数据库生成建议的语句
 * @author Administrator
 *
 */
public class AlterOrCreateTable {
	
	//文件路径
	public static final String inFile = "C:/inspur/xml/";//xml文件存放路径
	public static final String outFile = "C:/inspur/sql/";//sql文件生成路径
	
	private static Logger log = LogManager.getLogger(AlterOrCreateTable.class.getName());
	
	public static String createSql(String tableName,String dbType) {

		boolean mkdirFlag = MakeEmptyDir.doMake();
//		log.debug("创建空文件夹是否成功："+(mkdirFlag==true?"成功。":"失败！！！"));
		
		String retStr= "";//返回的信息
		//循环文件夹，找出里面需要生成建表语句的xml
		String fileName = "";
		List fileList = getFileList(inFile);
		if(fileList!=null && fileList.size()>0){
			for(int f=0;f<fileList.size();f++){
				String name = (String) fileList.get(f);//文件夹下的文件名称
				fileName = inFile + name;//生成文件名称
				String inxml = Dom4ParseXML.parseXML(fileName);
				try {
					List moduleList = Dom4ParseXML.getElementList(inxml,"app-data/database/subsystem", "module",
							Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
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
								String tblId = (String) tableMap.get("ID");
								tblId = tblId.trim();
								String tblName = (String) tableMap.get("NAME");
								if(tblId.equals(tableName)){
									retStr = getTableSql(tableMap,dbType).toString();
								}
							}
						}
					}
				}catch (Exception e) {
					log.debug("e==="+e.getMessage());
				}
			}
		}else{
			log.debug(inFile+"是空文件夹！！！");
		}
		return retStr;
	}
	
	/**
	 * @param tableName
	 * @param colName
	 * @param dbType
	 * @return
	 */
	public static String alterSql(String tableName, String colName,String dbType) {
		String retStr = "";
		//循环文件夹，找出里面需要生成建表语句的xml
		String fileName = "";
		List fileList = getFileList(inFile);
		if(fileList!=null && fileList.size()>0){
			for(int f=0;f<fileList.size();f++){
				String name = (String) fileList.get(f);//文件夹下的文件名称
				fileName = inFile + name;//生成文件名称
				String inxml = Dom4ParseXML.parseXML(fileName);
				try {
					List moduleList = Dom4ParseXML.getElementList(inxml,"app-data/database/subsystem", "module",
							Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
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
								String tblId = (String) tableMap.get("ID");
								tblId = tblId.trim();
								if(tblId.equals(tableName)){
									retStr = getAlterSql(tableMap,tableName,colName,dbType).toString();
								}
							}
						}
					}
				}catch (Exception e) {
					log.debug("e==="+e.getMessage());
				}
			}
		}else{
			log.debug(inFile+"是空文件夹！！！");
		}
		return retStr;
	}
	
	
	/**
	 * @param tableMap
	 * @param colName
	 * @param dbType
	 * @return
	 */
	public static StringBuffer getAlterSql(Map tableMap,String tabName,String comColName,String dbType) {

		List columnList = Dom4ParseXML.getElementList((String)tableMap.get("asXML"),"table", "column",Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
		String tblId = (String) tableMap.get("ID");
		tblId = tblId.trim();
		StringBuffer tblStrBuffer = new StringBuffer();
		if(tabName.equals(tblId)){
			tblStrBuffer.append("--"+"表："+tabName+"  字段："+comColName).append("\n");
			tblStrBuffer.append("ALTER TABLE "+tblId+" ADD ");
			for (int c=0;c<columnList.size();c++) {
				Map colMap = (Map) columnList.get(c);
				String colId = (String) (colMap.get("ID")==null?"":colMap.get("ID"));
				colId = colId.trim().toUpperCase();
				String colType = (String) (colMap.get("TYPE")==null?"":colMap.get("TYPE"));
				colType = colType.trim().toUpperCase();
				String colSize = (String) (colMap.get("SIZE")==null?"":colMap.get("SIZE"));
				colSize = colSize.trim().toUpperCase();
				String colDefault = (String) (colMap.get("DEFAULT")==null?"":colMap.get("DEFAULT"));
				colDefault = colDefault.trim().toUpperCase();
				String colRequired = (String) (colMap.get("REQUIRED")==null?"":colMap.get("REQUIRED"));
				colRequired = colRequired.trim().toUpperCase();
				if(colId.equals(comColName)){
					if(colId.equals("INTERVAL")){//特殊关键字
						if(dbType.equals("MySQL")){//MySQL
							tblStrBuffer.append(" `" + colId + "`  ");
						}else{
							tblStrBuffer.append(" " + colId + "  ");
						}
					}else{
						tblStrBuffer.append(" " + colId + "  ");
					}
					if ("TIMESTAMP".equals(colType)) {
						if(dbType.equals("DB2")){//DB2
							tblStrBuffer.append(colType+" WITH DEFAULT CURRENT TIMESTAMP ");
						}else if(dbType.equals("Oracle")){//Oracle
							tblStrBuffer.append(colType+" DEFAULT CURRENT_TIMESTAMP ");
						}else if(dbType.equals("MySQL")){//MYSQL
							tblStrBuffer.append(colType+" DEFAULT CURRENT_TIMESTAMP ");
						}
					}else{
						if("CHAR".equals(colType) || "VARCHAR".equals(colType)) {
							if(colSize==null || "".equals(colSize)){
								tblStrBuffer.append(colType + "(" + 1 + ")" + " ");
							}else{
								tblStrBuffer.append(colType + "(" + colSize + ")" + " ");
							}
						}else if("NUMERIC".equals(colType)) {
							tblStrBuffer.append("DECIMAL" + "(" + colSize + ")" + " ");
						}else{
							tblStrBuffer.append(colType + " ");
						}
						if(dbType.equals("Oracle")){//Oracle
							if(!"".equals(colDefault)){
								if("CHAR".equals(colType) || "VARCHAR".equals(colType)){
									tblStrBuffer.append(" DEFAULT '" + colDefault + "' ");
								}else{
									tblStrBuffer.append(" DEFAULT " + colDefault + " ");
								}
							}
							if("TRUE".equals(colRequired)){
								tblStrBuffer.append("NOT NULL ");
							}
						}else {
							if("TRUE".equals(colRequired)){
								tblStrBuffer.append("NOT NULL ");
							}
							if(!"".equals(colDefault)){
								if("CHAR".equals(colType) || "VARCHAR".equals(colType)){
									tblStrBuffer.append(" DEFAULT '" + colDefault + "' ");
								}else{
									tblStrBuffer.append(" DEFAULT " + colDefault + " ");
								}
							}
						}
						tblStrBuffer.append("; \n");
					}
				}
			}
		}
		return tblStrBuffer;
	}

	/**
	 * 简要说明：根据解析的xml table标签map生成建表语句
	 * @param tableMap
	 * @return
	 */
	public static StringBuffer getTableSql(Map tableMap,String dbType) {
		
		List columnList = Dom4ParseXML.getElementList((String) tableMap.get("asXML"), "table", "column",Dom4ParseXML.TYPE_NODE_VALUE_ATTR);
		String tblId = (String) tableMap.get("ID");
		tblId = tblId.trim();
		String tblName = (String) tableMap.get("NAME");
		String tblSpace = (String) (tableMap.get("TSP")==null?"":tableMap.get("TSP"));
		String tblIndexSpace = (String) (tableMap.get("IDXTSP")==null?"":tableMap.get("IDXTSP"));
		StringBuffer tblStrBuffer = new StringBuffer();
		if(!tblId.endsWith("_L") || !tblId.endsWith("_V") ){
			if(columnList.size()>0 && columnList!=null){
				tblStrBuffer.append("--" + tblName+":"+tblId).append("\n");
				tblStrBuffer.append("CREATE TABLE " + tblId).append("\n");
				tblStrBuffer.append("(").append("\n");
			}
			for (int c=0;c<columnList.size();c++) {
				Map colMap = (Map) columnList.get(c);
				String colId = (String) (colMap.get("ID")==null?"":colMap.get("ID"));
				String colName = (String) (colMap.get("NAME")==null?"":colMap.get("NAME"));
				String colType = (String) (colMap.get("TYPE")==null?"":colMap.get("TYPE"));
				colType = colType.trim().toUpperCase();
				String colSize = (String) (colMap.get("SIZE")==null?"":colMap.get("SIZE"));
				colSize = colSize.trim().toUpperCase();
				String colEnumValue = (String) (colMap.get("ENUMVALUE")==null?"":colMap.get("ENUMVALUE"));
				String colDefault = (String) (colMap.get("DEFAULT")==null?"":colMap.get("DEFAULT"));
				colDefault = colDefault.trim().toUpperCase();
				String colNote = (String) (colMap.get("NOTE")==null?"":colMap.get("NOTE"));
				String colRequired = (String) (colMap.get("REQUIRED")==null?"":colMap.get("REQUIRED"));
				colRequired = colRequired.trim().toUpperCase();
				if(colId.equals("INTERVAL")){//特殊关键字
					if(dbType.equals("MySQL")){//MySQL
						tblStrBuffer.append("	`" + colId + "`  ");
					}else{
						tblStrBuffer.append("	" + colId + "  ");
					}
				}else{
					tblStrBuffer.append("	" + colId + "  ");
				}
				if ("TIMESTAMP".equals(colType)) {
					if(dbType.equals("DB2")){//DB2
						tblStrBuffer.append(colType+" WITH DEFAULT CURRENT TIMESTAMP ");
					}else if(dbType.equals("Oracle")){//Oracle
						tblStrBuffer.append(colType+" DEFAULT CURRENT_TIMESTAMP ");
					}else if(dbType.equals("MySQL")){//MySQL
						tblStrBuffer.append(colType+" DEFAULT CURRENT_TIMESTAMP ");
					}
				}else{
					if("CHAR".equals(colType) || "VARCHAR".equals(colType)) {
						if(colSize==null || "".equals(colSize)){
							tblStrBuffer.append(colType + "(" + 1 + ")" + " ");
						}else{
							tblStrBuffer.append(colType + "(" + colSize + ")" + " ");
						}
					}else if("NUMERIC".equals(colType)) {
						tblStrBuffer.append("DECIMAL" + "(" + colSize + ")" + " ");
					}else{
						tblStrBuffer.append(colType + " ");
					}
					if(dbType.equals("Oracle")){//Oracle
						if(!"".equals(colDefault)){
							if("CHAR".equals(colType) || "VARCHAR".equals(colType)){
								tblStrBuffer.append(" DEFAULT '" + colDefault + "' ");
							}else{
								tblStrBuffer.append(" DEFAULT " + colDefault + " ");
							}
						}
						if("TRUE".equals(colRequired)){
							tblStrBuffer.append("NOT NULL ");
						}
					}else {
						if("TRUE".equals(colRequired)){
							tblStrBuffer.append("NOT NULL ");
						}
						if(!"".equals(colDefault)){
							if("CHAR".equals(colType) || "VARCHAR".equals(colType)){
								tblStrBuffer.append(" DEFAULT '" + colDefault + "' ");
							}else{
								tblStrBuffer.append(" DEFAULT " + colDefault + " ");
							}
						}
					}
				}
				if(c != (columnList.size()-1)){
					tblStrBuffer.append(", ");
				}
				tblStrBuffer.append("--"+colName+" "+colNote+"  "+colEnumValue).append("\n");
			}
			if(columnList.size()>0 && columnList!=null){
				if(dbType.equals("DB2")){//DB2
					tblStrBuffer.append(") IN \""+tblSpace+"\" INDEX IN \""+tblIndexSpace+"\" ;").append("\n");
				}else if(dbType.equals("Oracle")){//Oracle
					tblStrBuffer.append(") TABLESPACE "+tblSpace+" ; \n");
				}else if(dbType.equals("MySQL")){//MySQL
					tblStrBuffer.append(") ENGINE=InnoDB DEFAULT CHARSET=gbk ; \n");
				}
			}
			//找出主键
			String priStr = "";
			for (int c=0;c<columnList.size();c++) {
				Map colMap = (Map) columnList.get(c);
				String colId = (String) colMap.get("ID");
				String colPri = (String) (colMap.get("PRIMARYKEY") == null?"":colMap.get("PRIMARYKEY"));
				colPri = colPri.trim().toUpperCase();
				if ("TRUE".equals(colPri)) {
					priStr += colId + ",";
				}
			}
			priStr = "".equals(priStr)?"":priStr.substring(0,priStr.length()-1);//去除最后一个逗号
			if (!"".equals(priStr)) {
				tblStrBuffer.append("--声明主键").append("\n");
				tblStrBuffer.append("ALTER TABLE "+tblId +" ADD CONSTRAINT "+tblId+"_PK PRIMARY KEY (");
				tblStrBuffer.append(priStr).append(")");
				if(dbType.equals("DB2")){//DB2
					tblStrBuffer.append(" ;").append("\n");
				}else if(dbType.equals("Oracle")){//Oracle
					tblStrBuffer.append(" USING INDEX TABLESPACE TD_OTHER_IDX;").append("\n");
				}else if(dbType.equals("MySQL")){//MySQL
					tblStrBuffer.append(" ;").append("\n");
				}
			}
			for(int m=0;m<columnList.size();m++){
				Map colMap = (Map) columnList.get(m);
				String colId = (String) colMap.get("ID");
				colId = colId.trim().toUpperCase();
				String note = (String) (colMap.get("NOTE") == null?"":colMap.get("NOTE"));
				note = note.trim();
				if (note.contains("数据库自增")) {
					tblStrBuffer.append("----修改数据库自增字段").append("\n");
					if(dbType.equals("DB2")){//DB2
						tblStrBuffer.append("ALTER TABLE "+tblId+" ALTER COLUMN "+colId
								+" SET GENERATED ALWAYS AS IDENTITY (START WITH 1,INCREMENT BY 1) ; \n");
					}else if(dbType.equals("Oracle")){//Oracle
						tblStrBuffer.append("CREATE SEQUENCE "+tblId+"_SEQ \n");
						tblStrBuffer.append("INCREMENT BY 1 START WITH 1 \n");
						tblStrBuffer.append("MAXVALUE 999999999999999999999999 \n");
						tblStrBuffer.append("MINVALUE 1 NOCYCLE CACHE 200 NOORDER ; \n");
						tblStrBuffer.append("CREATE OR REPLACE TRIGGER "+tblId+"_TRIG \n");
						tblStrBuffer.append("  BEFORE INSERT ON "+tblId+" FOR EACH ROW \n");
						tblStrBuffer.append(" BEGIN \n");
						tblStrBuffer.append("  SELECT "+tblId+"_SEQ.NEXTVAL INTO :NEW."+colId+" FROM DUAL; \n");
						tblStrBuffer.append("END; \n");
					}else if(dbType.equals("MySQL")){//MySQL
						tblStrBuffer.append("ALTER TABLE "+tblId+" CHANGE "+colId+" "+colId+
								" BIGINT(24) NOT NULL AUTO_INCREMENT; \n");
					}
				}
			}
		}
		
		return tblStrBuffer;
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
	
}