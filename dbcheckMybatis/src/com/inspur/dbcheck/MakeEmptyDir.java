package com.inspur.dbcheck;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MakeEmptyDir {

	
	private static Logger log = LogManager.getLogger(MakeEmptyDir.class.getName());//定义日志变量
	/**
	 * 测试类
	 * @param args
	 */
	public static void main(String[] args) {
		boolean mkdirFlag = MakeEmptyDir.doMake();
		log.debug("创建空文件夹是否成功："+(mkdirFlag==true?"成功。":"失败！！！"));
		
	}
	/**
	 * 生成空文件夹，用于存放日志，放置报错
	 * @return
	 */
	public static boolean doMake() {
		boolean flag = false;
		String path = "C:/inspur/";
		String xmlPath = path+"xml/";
		String logPath = path+"logs/";
		String sqlPath = path+"sql/";
		File pathFile = new File(path);
		int num = 0;
		if(!pathFile.exists() && !pathFile.isDirectory()){
			pathFile.mkdir();
			num = 1;
		}
		
		File xmlPathFile = new File(xmlPath);
		if(!xmlPathFile.exists() && !xmlPathFile.isDirectory()){
			xmlPathFile.mkdir();
			num = 2;
		}
		
		File logPathFile = new File(logPath);
		if(!logPathFile.exists() && !logPathFile.isDirectory()){
			logPathFile.mkdir();
			num = 3;
		}
		
		File sqlPathFile = new File(sqlPath);
		if(!sqlPathFile.exists() && !sqlPathFile.isDirectory()){
			sqlPathFile.mkdir();
			num = 4;
		}
		if(num==4){
			flag = true;
		}
		return flag;
	}

}
