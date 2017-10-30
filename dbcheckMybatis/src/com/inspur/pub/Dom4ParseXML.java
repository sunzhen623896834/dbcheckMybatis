package com.inspur.pub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.inspur.dbcheck.CreateTableSql;

/**
 * 运用Dom4j解析XML
 * @author Administrator
 *
 */
public class Dom4ParseXML {
	
	private static Log log = LogFactory.getLog(CreateTableSql.class);
	
	/**
	 * 标签的属性值组成节点Map
	 */
	public static final String TYPE_NODE_VALUE_ATTR = "ATTR";
	/**
	 * 标签下的子标签值组成节点Map
	 */
	public static final String TYPE_NODE_VALUE_SUBNODE = "SUBNODE";
	public static final String ENCODE_UTF8 = "UTF-8";
	public static final String ENCODE_GBK = "GBK";
	public static final String ENCODE_GB2312 = "GB2312";

	/**
	 * 
	 * 简要说明：从文件中读取xml字符串
	 * @param fileName：文件路径
	 * @return String
	 */
	public static String parseXML(String fileName) {
		String reStr = "";
		try {
			String filePath = "C:/inspur/V6_Schema.dtd";
			File file1 = new File(filePath);
			//先生成空文件
    		if (file1.isFile() && file1.exists()) {
//    			file1.delete();
    		}else {
    			new BufferedWriter(new FileWriter(new File(filePath), true));
			}
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(new File(fileName));
			Element rootElement = document.getRootElement();
			reStr = rootElement.asXML();
			if (file1.isFile() && file1.exists()) {//删除生成的空文件
    			file1.delete();
    		}
		} catch (Exception e) {
			String filePath = "C:/inspur/V6_Schema.dtd";
			File file1 = new File(filePath);
			if (file1.isFile() && file1.exists()) {//删除生成的空文件
    			file1.delete();
    		}
			log.debug("e=="+e.getMessage());
		}
		return reStr;
	}
	
	/**
	 * 
	 * 简要说明：从文件中读取xml字符串
	 * @param fileName：文件路径
	 * @param encode：编码格式
	 * @return String
	 */
	public static String parseXMLBack(String fileName, String encode) {
		String reStr = "";
		try {
			InputStream ifile = new FileInputStream(fileName);
			InputStreamReader inputXml = new InputStreamReader(ifile, encode);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			Element rootElement = document.getRootElement();
			reStr = rootElement.asXML();
		} catch (Exception e) {
			log.debug("e=="+e.getMessage());
		}
		return reStr;
	}

	/**
	 * 
	 * @param xmlStr：xml字符串
	 * @param elementName：节点名称，多层节点用 '/' 隔开 eg:root/country/city       
	 * @param valueType
	 *            值类型： TYPE_NODE_VALUE_ATTR（标签的属性值组成节点Map）
	 *            TYPE_NODE_VALUE_SUBNODE（标签下的子标签值组成节点Map）
	 * @return Map
	 */
	public static Map getElementAttrValueMap(String xmlStr, String elementName,
			String valueType) {
		Map nodeMap = new HashMap();
		// 参数为空直接返回
		if ((xmlStr==null || "".equals(xmlStr)) || ("".equals(elementName) || null==elementName)) {
			return nodeMap;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		String[] eleArr = elementName.split("\\/");
		try {
			Document document = DocumentHelper.parseText(xmlStr);
			Element rootElement = document.getRootElement();
			if (eleArr[0].equals(rootElement.getName())) {
				if (eleArr.length == 1) {
					Element element = rootElement;
					if (TYPE_NODE_VALUE_ATTR.equals(valueType)) {
						for (Iterator attrI = element.attributeIterator();attrI.hasNext();) {
							Attribute attr = (Attribute) attrI.next();
							nodeMap.put(attr.getName().toUpperCase(),attr.getValue());
						}
					} else {
						for (Iterator attrI = element.elementIterator();attrI.hasNext();) {
							Element subEle = (Element) attrI.next();
							nodeMap.put(subEle.getName().toUpperCase(),subEle.getText());
						}
					}

				} else {
					for (Iterator eleI = rootElement.elementIterator();eleI.hasNext();) {
						Element element = (Element) eleI.next();
						if (eleArr[1].equals(element.getName())) {
							if (1 == (eleArr.length - 1)) {
								if (TYPE_NODE_VALUE_ATTR.equals(valueType)) {
									for (Iterator attrI = element.attributeIterator();attrI.hasNext();) {
										Attribute attr = (Attribute) attrI.next();
										nodeMap.put(attr.getName().toUpperCase(), attr.getValue());
									}
								} else {
									for (Iterator attrI = element.elementIterator();attrI.hasNext();) {
										Element subEle = (Element) attrI.next();
										nodeMap.put(subEle.getName().toUpperCase(),subEle.getText());
									}
								}
							} else {
								nodeMap = getElementAttrValueMap(element.asXML(),elementName.substring(elementName.indexOf("/") + 1), valueType);
							}
							break;
						}
					}
				}
			} else {
				return nodeMap;
			}
		} catch (DocumentException e) {
			 e.printStackTrace();
		}
		return nodeMap;
	}

	/**
	 * 
	 * 简要说明：获取某个节点的xml字符串
	 * @param xmlStr：xml字符串
	 * @param elementName：节点名称，多层节点用 '/' 隔开 eg:root/country/city
	 * @return String
	 */
	public static String getElementXMLText(String xmlStr,String elementName) {
		String reStr = "";
		// 参数为空直接返回
		if ((xmlStr == null || "".equals(xmlStr)) || ("".equals(elementName) || null == elementName)) {
			return reStr;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		String[] eleArr = elementName.split("/");
		try{
			Document document = DocumentHelper.parseText(xmlStr);
			Element rootElement = document.getRootElement();
			if(eleArr[0].equals(rootElement.getName())){
				if(eleArr.length == 1) {
					reStr = rootElement.asXML();
				}else{
					for(Iterator eleI = rootElement.elementIterator();eleI.hasNext();){
						Element element = (Element) eleI.next();
						if(eleArr[1].equals(element.getName())) {
							if(1 == (eleArr.length - 1)) {
								reStr = element.asXML();
							}else{
								reStr = getElementXMLText(element.asXML(),elementName.substring(elementName.indexOf("/") + 1));
							}
							break;
						}
					}
				}
			} else {
				return reStr;
			}
		}catch(DocumentException e) {
			e.printStackTrace();
		}
		return reStr;
	}

	/**
	 * 
	 * 简要说明：获取某个节点的值
	 * @param xmlStr：xml字符串
	 * @param elementName：节点名称，多层节点用 '/' 隔开 eg:root/country/city
	 * @return String
	 */
	public static String getElementText(String xmlStr, String elementName) {
		String reStr = "";
		// 参数为空直接返回
		if((xmlStr == null || "".equals(xmlStr)) || ("".equals(elementName) || null == elementName)) {
			return reStr;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		String[] eleArr = elementName.split("/");
		try{
			Document document = DocumentHelper.parseText(xmlStr);
			Element rootElement = document.getRootElement();
			if(eleArr[0].equals(rootElement.getName())){
				if(eleArr.length == 1) {
					reStr = rootElement.getText();
				}else{
					for(Iterator eleI = rootElement.elementIterator(); eleI.hasNext();){
						Element element = (Element) eleI.next();
						if(eleArr[1].equals(element.getName())){
							if(1 == (eleArr.length - 1)){
								reStr = element.getText();
							}else{
								reStr = getElementText(element.asXML(),elementName.substring(elementName.indexOf("/") + 1));
							}
							break;
						}
					}
				}
			}else{
				return reStr;
			}
		}catch(DocumentException e) {
			e.printStackTrace();
		}
		return reStr;
	}

	/**
	 * 
	 * 简要说明：获取xml中指定父节点下的子节点List，每一个子节点的属性值通过Map存储
	 * 
	 * @param xmlStr：xml字符串
	 * @param fatherNode：父节点名称 多层节点用 '/' 隔开 eg: eg:root/country
	 * @param subNode：子节点名称 
	 * @param valueType
	 *            值类型： TYPE_NODE_VALUE_ATTR（标签的属性值组成节点Map）
	 *            TYPE_NODE_VALUE_SUBNODE（标签下的子标签值组成节点Map）
	 * @return List
	 */
	public static List getElementList(String xmlStr, String fatherNode,
			String subNode, String valueType) {
		List retList = new ArrayList();
		if("".equals(xmlStr) || "".equals(fatherNode) || ("".equals(subNode) && fatherNode.indexOf("/")<0)) {
			return retList;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		if("".equals(subNode)) {
			subNode = fatherNode.substring(fatherNode.lastIndexOf("/") + 1);
			fatherNode = fatherNode.indexOf("/")>=0?fatherNode.substring(0,fatherNode.lastIndexOf("/")):fatherNode;
		}
		String subXmlStr = getElementXMLText(xmlStr, fatherNode);
		retList = getNodeDataList(subXmlStr, subNode, valueType);
		return retList;
	}

	/**
	 * 
	 * 简要说明：获取某一段xml下的指定节点的List，属性值通过Map存储
	 * @param xmlStr：xml字符串
	 * @param nodeName：节点名称
	 * @param valueType
	 *            值类型： TYPE_NODE_VALUE_ATTR（标签的属性值组成节点Map）
	 *            TYPE_NODE_VALUE_SUBNODE（标签下的子标签值组成节点Map）
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	private static List getNodeDataList(String xmlStr, String nodeName,
			String valueType) {
		List retList = new ArrayList();
		if (nodeName == null || "".equals(nodeName)) {
			return retList;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		try {
			Document document = DocumentHelper.parseText(xmlStr);
			Element rootElement = document.getRootElement();
			for (Iterator eleI = rootElement.elementIterator(); eleI.hasNext();) {
				Element element = (Element) eleI.next();

				if (nodeName.equals(element.getName())) {
					Map nodeMap = new HashMap();
					if(TYPE_NODE_VALUE_ATTR.equals(valueType)){
						for (Iterator attrI = element.attributeIterator();attrI.hasNext();){
							Attribute attr = (Attribute) attrI.next();
							nodeMap.put(attr.getName().toUpperCase(),attr.getValue());
						}
					}else{
						for(Iterator attrI = element.elementIterator(); attrI.hasNext();){
							Element subEle = (Element) attrI.next();
							if(null == nodeMap.get(subEle.getName().toUpperCase())){
							}
							nodeMap.put(subEle.getName().toUpperCase(),
									"".equals(subEle.getText().trim())?("".equals(subEle.getText())?"":subEle.asXML()):subEle.getText());
						}
					}
					nodeMap.put("asXML", element.asXML());
					retList.add(nodeMap);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return retList;
	}

	/**
	 * 
	 * 简要说明：获取某个节点某个属性的值
	 * @param xmlStr：xml字符串
	 * @param elementName：节点名称，多层节点用 '/' 隔开 eg:root/country/city  
	 * @param attrName：属性名称      
	 * @return String
	 */
	public static String getElementAttr(String xmlStr, String elementName,
			String attrName) {
		String reStr = "";
		// 参数为空直接返回
		if ((xmlStr == null || "".equals(xmlStr))
				|| ("".equals(elementName) || null == elementName)
				|| (attrName == null || "".equals(attrName))) {
			return reStr;
		}
		xmlStr = xmlStr.substring(xmlStr.indexOf("<")<0?0:xmlStr.indexOf("<"));
		String[] eleArr = elementName.split("/");
		try {
			Document document = DocumentHelper.parseText(xmlStr);
			Element rootElement = document.getRootElement();
			if (eleArr[0].equals(rootElement.getName())) {
				if (eleArr.length == 1) {
					reStr = rootElement.attributeValue(attrName);
				} else {
					for (Iterator eleI = rootElement.elementIterator(); eleI.hasNext();) {
						Element element = (Element) eleI.next();
						if (eleArr[1].equals(element.getName())) {
							if (1 == (eleArr.length - 1)) {
								reStr = element.attributeValue(attrName);
							} else {
								reStr = getElementAttr(element.asXML(),elementName.substring(elementName.indexOf("/") + 1), attrName);
							}
							break;
						}
					}
				}
			} else {
				return reStr;
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return reStr;
	}
}