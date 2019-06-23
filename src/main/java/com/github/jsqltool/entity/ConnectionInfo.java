package com.github.jsqltool.entity;

public class ConnectionInfo {

	private String name;
	private String driverClassName;
	private String url;
	private String userName;
	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static ConnectionInfo builderDefaultMysqlInfo() {
		ConnectionInfo info = new ConnectionInfo();
		String name = "测试MySQL";
		String className = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306";
		String username = "root";
		String password = "123456";

		info.setName(name);
		info.setDriverClassName(className);
		info.setUrl(url);
		info.setPassword(password);
		info.setUserName(username);

		return info;
	}

	public static ConnectionInfo builderDefaultOracleInfo() {
		ConnectionInfo info = new ConnectionInfo();
		String name = "测试Oracle";
		// 如果使用Oracle8i JDBC驱动程序，那么需要导入oracle.jdbc.driver.OracleDriver类
		// String className = "oracle.jdbc.driver.OracleDriver";
		String className = "oracle.jdbc.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost:1521:orcl";
		String username = "scott";
		String password = "123456";

		info.setName(name);
		info.setDriverClassName(className);
		info.setUrl(url);
		info.setPassword(password);
		info.setUserName(username);

		return info;
	}

}
