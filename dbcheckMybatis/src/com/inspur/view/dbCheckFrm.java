package com.inspur.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.inspur.dbcheck.CreateTableSql;
import com.inspur.dbcheck.DataStructCheck;
import com.inspur.dbcheck.MakeEmptyDir;
import com.inspur.dbcheck.WriteCheckTable;
import com.inspur.mybatis.MyBatisUtil;

import javax.swing.JTextArea;
import javax.swing.ImageIcon;

import java.awt.Color;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
/**
 * gis配送数据校验工具界面程序
 * @author Administrator
 * @date 2016-12-30
 *
 */
public class dbCheckFrm extends JFrame {

	private static Logger log = LogManager.getLogger(dbCheckFrm.class.getName());//定义日志变量
	private JPanel contentPane;
	private static JTextField dbipTxt;
	private static JTextField dbPortTxt;
	private static JTextField dbNameTxt;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private static JTextField dbUserTxt;
	private static JPasswordField dbPasswordTxt;
	private static JRadioButton db2RadioBtn;
	private static JRadioButton oracleRadioBtn;
	private static JRadioButton mysqlRadioBtn;
	private static JComboBox moduleBox;
	private final ButtonGroup appButtonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MakeEmptyDir.doMake();
					dbCheckFrm frame = new dbCheckFrm();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public dbCheckFrm() {
		setResizable(false);
		setTitle("数据结构校验工具(author：孙震1613，sunzhenrj@inspur.com)-v1.0-20171027");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 648, 541);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(224, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel dbTypeLabel = new JLabel("数据库类型：");
		dbTypeLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JLabel dbIpLabel = new JLabel("数据库IP  ：");
		dbIpLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		dbipTxt = new JTextField();
		dbipTxt.setColumns(10);
		
		JLabel dbPortLabel = new JLabel("数据库端口：");
		dbPortLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		dbPortTxt = new JTextField();
		dbPortTxt.setColumns(10);
		
		JLabel dbNameLabel = new JLabel("数据库名称：");
		dbNameLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		dbNameTxt = new JTextField();
		dbNameTxt.setColumns(10);
		
		db2RadioBtn = new JRadioButton("DB2");
		db2RadioBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dbPortTxt.setText("50000");
			}
		});
		buttonGroup.add(db2RadioBtn);
		db2RadioBtn.setSelected(true);
		
		
		oracleRadioBtn = new JRadioButton("Oracle");
		oracleRadioBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dbPortTxt.setText("1521");
			}
		});
		buttonGroup.add(oracleRadioBtn);
		
		
		mysqlRadioBtn = new JRadioButton("MySQL");
		mysqlRadioBtn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dbPortTxt.setText("3306");
			}
		});
		buttonGroup.add(mysqlRadioBtn);
		
		
		JLabel dbUserLabel = new JLabel("用  户  名：");
		dbUserLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		dbUserTxt = new JTextField();
		dbUserTxt.setColumns(10);
		
		JLabel dbPasswordLabel = new JLabel("密      码：");
		dbPasswordLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		
		dbPasswordTxt = new JPasswordField();
		
		JLabel dbIpLabel_ip = new JLabel("示例：10.110.1.210");
		dbIpLabel_ip.setBackground(Color.WHITE);
		dbIpLabel_ip.setForeground(Color.red);
		
		JLabel dbPortLabel_port = new JLabel("Oracle:1521  DB2:50000  MySQL:3306");
		dbPortLabel_port.setForeground(Color.red);
		
		JButton testConnBtn = new JButton("测试连接");
		testConnBtn.setIcon(new ImageIcon(dbCheckFrm.class.getResource("/com/images/network.png")));
		testConnBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testConnActionPerformed(e);
			}
		});
		
		JLabel moduleTxt = new JLabel("组      件：");
		moduleTxt.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JButton createTableSqlBtn = new JButton("生成建表语句");
		createTableSqlBtn.setIcon(new ImageIcon(dbCheckFrm.class.getResource("/com/images/Table.png")));
		createTableSqlBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateTableSqlActionPerformed(e);
			}
		});
		
		JButton writeCheckTableBtn = new JButton("写入校验标准表");
		writeCheckTableBtn.setIcon(new ImageIcon(dbCheckFrm.class.getResource("/com/images/write.png")));
		writeCheckTableBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WriteCheckTableActionPerformed(e);
			}
		});
		
		JButton DataStructCheckBtn = new JButton("校验数据结构");
		DataStructCheckBtn.setIcon(new ImageIcon(dbCheckFrm.class.getResource("/com/images/check.png")));
		DataStructCheckBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataStructCheckActionPerformed(e);
			}
		});
		
		JLabel userReaderLabel = new JLabel("使用说明：");
		userReaderLabel.setBackground(new Color(255, 255, 0));
		userReaderLabel.setFont(new Font("黑体", Font.PLAIN, 14));
		userReaderLabel.setForeground(new Color(255, 0, 0));
		
		JLabel lblGis = new JLabel("GIS配送数据校验工具");
		lblGis.setIcon(new ImageIcon(dbCheckFrm.class.getResource("/com/images/gis.png")));
		lblGis.setFont(new Font("新宋体", Font.PLAIN, 19));
		
		JTextArea txtrxmlccreatesqlxml = new JTextArea();
		txtrxmlccreatesqlxml.setForeground(new Color(47, 79, 79));
		txtrxmlccreatesqlxml.setBackground(new Color(255, 228, 181));
		txtrxmlccreatesqlxml.setFont(new Font("宋体", Font.PLAIN, 13));
		txtrxmlccreatesqlxml.setEditable(false);
		txtrxmlccreatesqlxml.setText("1、提供账号需要具有读写数据库权限；\r\n2、把xml文件放到C:\\inspur\\xml目录下；\r\n3、按照测试连接、写入校验标准表、校验数据结构的顺序执行；\r\n4、生成的文件位于C:\\inspur\\sql目录下；\r\n5、xml文件请咨询GIS配送组。");
		
		//增加组件，在此处增加即可
		moduleBox = new JComboBox();
		moduleBox.setModel(new DefaultComboBoxModel(new String[] {"LDM", "GIS"}));
		moduleBox.setEditable(false);
		moduleBox.setToolTipText("组件");
		
        contentPane.add(moduleBox);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(82)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(dbTypeLabel)
								.addComponent(dbIpLabel)
								.addComponent(dbPortLabel)
								.addComponent(dbNameLabel)
								.addComponent(dbUserLabel)
								.addComponent(dbPasswordLabel)
								.addComponent(moduleTxt))
							.addGap(18)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(moduleBox, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(db2RadioBtn)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(oracleRadioBtn)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(mysqlRadioBtn))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
										.addComponent(dbNameTxt)
										.addComponent(dbPortTxt)
										.addComponent(dbipTxt, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGap(17)
											.addComponent(dbIpLabel_ip))
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGap(18)
											.addComponent(dbPortLabel_port))))
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
									.addComponent(dbPasswordTxt, Alignment.LEADING)
									.addComponent(dbUserTxt, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(48)
							.addComponent(testConnBtn)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(createTableSqlBtn)
							.addGap(18)
							.addComponent(writeCheckTableBtn)
							.addGap(18)
							.addComponent(DataStructCheckBtn))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(29)
							.addComponent(userReaderLabel)
							.addGap(18)
							.addComponent(txtrxmlccreatesqlxml, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(217)
							.addComponent(lblGis)))
					.addContainerGap(28, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(9)
					.addComponent(lblGis)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbTypeLabel)
						.addComponent(db2RadioBtn)
						.addComponent(oracleRadioBtn)
						.addComponent(mysqlRadioBtn))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbIpLabel)
						.addComponent(dbipTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(dbIpLabel_ip))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbPortLabel)
						.addComponent(dbPortTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(dbPortLabel_port))
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbNameLabel)
						.addComponent(dbNameTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbUserLabel)
						.addComponent(dbUserTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(dbPasswordLabel)
						.addComponent(dbPasswordTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(moduleTxt)
						.addComponent(moduleBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(30)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(testConnBtn)
						.addComponent(createTableSqlBtn)
						.addComponent(writeCheckTableBtn)
						.addComponent(DataStructCheckBtn))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(18)
							.addComponent(userReaderLabel))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(txtrxmlccreatesqlxml, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(28, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	/**
	 * 校验数据结构
	 * @param e
	 */
	public void DataStructCheckActionPerformed(ActionEvent e) {

		String dbType = getDbType();
		String ip = dbipTxt.getText().trim();
		String port = dbPortTxt.getText().trim();
		String dbName = dbNameTxt.getText().trim();
		String user = dbUserTxt.getText().trim();
		String password = new String(dbPasswordTxt.getPassword()).trim();
		String module = getSelectModule();//组件
		String url = "";
		if(dbType.equals("DB2")){
			url = "jdbc:db2://"+ip+":"+port+"/"+dbName;
		}else if(dbType.equals("Oracle")){
			url = "jdbc:oracle:thin:@"+ip+":"+port+":"+dbName;
		}else if(dbType.equals("MySQL")){
			url = "jdbc:mysql://"+ip+":"+port+"/"+dbName+"?useSSL=true";
		}
		boolean connFlag = testDbConnection();
		if(connFlag==true){
			boolean flag = DataStructCheck.doDataStructCheck(url,user,password,dbType,module);
			if(flag==true){
				JOptionPane.showMessageDialog(null, "校验完成，请到C:/inspur/sql目录下去查找。");
			}
		}
	}

	/** 写入标准校验表
	 * @param e
	 */
	public void WriteCheckTableActionPerformed(ActionEvent e) {
		String dbType = getDbType();
		String ip = dbipTxt.getText().trim();
		String port = dbPortTxt.getText().trim();
		String dbName = dbNameTxt.getText().trim();
		String user = dbUserTxt.getText().trim();
		String password = new String(dbPasswordTxt.getPassword()).trim();
		String module = getSelectModule();//组件
		String url = "";
		if(dbType.equals("DB2")){
			url = "jdbc:db2://"+ip+":"+port+"/"+dbName;
		}else if(dbType.equals("Oracle")){
			url = "jdbc:oracle:thin:@"+ip+":"+port+":"+dbName;
		}else if(dbType.equals("MySQL")){
			url = "jdbc:mysql://"+ip+":"+port+"/"+dbName+"?useSSL=true";
		}
		boolean connFlag = testDbConnection();
		if(connFlag==true){
			boolean flag = WriteCheckTable.doWriteCheckTable(url, user, password, dbType,module);
			if(flag==true){
				JOptionPane.showMessageDialog(null, "写入标准校验表成功，可以进行数据结构的校验了！");
			}
		}
		
	}
	
	public static boolean testDbConnection(){
		String dbType = getDbType();
		String ip = dbipTxt.getText().trim();
		String port = dbPortTxt.getText().trim();
		String dbName = dbNameTxt.getText().trim();
		String user = dbUserTxt.getText().trim();
		String password = new String(dbPasswordTxt.getPassword()).trim();
		
		boolean ipFlag = checkNull(ip);
		boolean portFlag = checkNull(port);
		boolean dbNameFlag = checkNull(dbName);
		boolean userFlag = checkNull(user);
		boolean passwordFlag = checkNull(password);

		if(ipFlag==true){
			JOptionPane.showMessageDialog(null, "数据库IP地址不能为空！");
			return false;
		}else if(!ipCheck(ip)){
			JOptionPane.showMessageDialog(null, "数据库IP地址不合法，请重新输入！");
			return false;
		}else if(portFlag==true){
			JOptionPane.showMessageDialog(null, "数据库端口号不能为空！");
			return false;
		}else if(dbNameFlag==true){
			JOptionPane.showMessageDialog(null, "数据库名称不能为空！");
			return false;
		}else if(userFlag==true){
			JOptionPane.showMessageDialog(null, "数据库用户名不能为空！");
			return false;
		}else if(passwordFlag==true){
			JOptionPane.showMessageDialog(null, "数据库密码不能为空！");
			return false;
		}
		String url = "";
		if(dbType.equals("DB2")){
			url = "jdbc:db2://"+ip+":"+port+"/"+dbName;
		}else if(dbType.equals("Oracle")){
			url = "jdbc:oracle:thin:@"+ip+":"+port+":"+dbName;
		}else if(dbType.equals("MySQL")){
			url = "jdbc:mysql://"+ip+":"+port+"/"+dbName+"?useSSL=true";
		}
		boolean dbConnFlag = false;
		try {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, password, dbType, true);
			dbConnFlag = true;
		} catch (Exception e) {
			log.debug("e=="+e.getMessage());
		}
		return dbConnFlag;
	}

	/** 生成建表语句
	 * @param e
	 */
	public void CreateTableSqlActionPerformed(ActionEvent e) {
		String dbType = getDbType();
		boolean flag = CreateTableSql.doCreateTableSql(dbType);
		if(flag==true){
			JOptionPane.showMessageDialog(null, "生成sql成功，此sql仅供参考！");
		}
	}

	/**
	 * 测试数据库连接
	 * @param e
	 */
	public void testConnActionPerformed(ActionEvent e) {

		log.debug("testConnActionPerformed--start");
		String dbType = getDbType().trim();
		String ip = dbipTxt.getText().trim();
		String port = dbPortTxt.getText().trim();
		String dbName = dbNameTxt.getText().trim();
		String user = dbUserTxt.getText().trim();
		String password = new String(dbPasswordTxt.getPassword()).trim();
		
		boolean ipFlag = checkNull(ip);
		boolean portFlag = checkNull(port);
		boolean dbNameFlag = checkNull(dbName);
		boolean userFlag = checkNull(user);
		boolean passwordFlag = checkNull(password);

		if(ipFlag==true){
			JOptionPane.showMessageDialog(null, "数据库IP地址不能为空！");
			return;
		}else if(!ipCheck(ip)){
			JOptionPane.showMessageDialog(null, "数据库IP地址不合法，请重新输入！");
			return;
		}else if(portFlag==true){
			JOptionPane.showMessageDialog(null, "数据库端口号不能为空！");
			return;
		}else if(dbNameFlag==true){
			JOptionPane.showMessageDialog(null, "数据库名称不能为空！");
			return;
		}else if(userFlag==true){
			JOptionPane.showMessageDialog(null, "数据库用户名不能为空！");
			return;
		}else if(passwordFlag==true){
			JOptionPane.showMessageDialog(null, "数据库密码不能为空！");
			return;
		}
		String url = "";
		if(dbType.equals("DB2")){
			url = "jdbc:db2://"+ip+":"+port+"/"+dbName;
		}else if(dbType.equals("Oracle")){
			url = "jdbc:oracle:thin:@"+ip+":"+port+":"+dbName;
		}else if(dbType.equals("MySQL")){
			url = "jdbc:mysql://"+ip+":"+port+"/"+dbName+"?useSSL=true";
		}
		log.debug("前期准备完成。。。。");
		boolean flag = false;
		try {
			SqlSession sqlSession = MyBatisUtil.getSqlSession(url, user, password, dbType, true);
			flag = true;
		} catch (Exception e2) {
			log.debug("e2=="+e2.getMessage());
		}
		log.debug("测试数据库连接标志："+flag);
		log.debug("建立数据库连接完成。。。");
		if(flag==true){
			JOptionPane.showMessageDialog(null, "数据库连接测试成功！");
		}else{
			JOptionPane.showMessageDialog(null, "数据库连接测试失败！请检查输入信息","出错了", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** 获取选择的组件
	 * @return
	 */
	public static String getSelectModule() {
		String module = (String) moduleBox.getSelectedItem();
		return module;
	}

	/**
	 * 校验字符是否为空，如果为空的话，则返回true，否则false
	 * @param str
	 * @return
	 */
	public static boolean checkNull(String str){
		boolean flag = false;
		if(str==null || str.trim().equals("")){
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 获取选择的数据库类型
	 * @return
	 */
	public static String getDbType(){
		//数据库类型
		String dbType = "";
		if(db2RadioBtn.isSelected()){
			dbType = "DB2";
		}else if(oracleRadioBtn.isSelected()){
			dbType = "Oracle";
		}else if(mysqlRadioBtn.isSelected()){
			dbType = "MySQL";
		}
		return dbType;
	}
	
	/**
	 * 校验输入的ip是否合法
	 * @param text
	 * @return
	 */
	public static boolean ipCheck(String text) {
		boolean flag = false;
        if(text != null && !text.isEmpty()){
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if(text.matches(regex)){
            	flag = true;
            }else{
            	flag = false;
            }
        }
        return flag;
    }
}
