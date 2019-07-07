package com.github.jsqltool.conn;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.enums.DBType;

public class DbConnection {

	private int dbType;
	private String name;
	private String className;
	private String url;
	private String username;
	private String password;
	private boolean autoCommit;
	private int isolationLevel;
	private boolean readOnly;
	private String catalog;
	private boolean quotes;

	public DbConnection(ConnectionInfo info) {
		this.dbType = DBType.getDBTypeByUrl(info.getUrl()).ordinal();
		this.name = info.getName();
		this.className = info.getDriverClassName();
		this.url = info.getUrl();
		this.username = info.getUserName();
		this.password = info.getPassword();
		this.autoCommit = true;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getIsolationLevel() {
		return isolationLevel;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public int getDbType() {
		return dbType;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setDbType(int dbType) {
		this.dbType = dbType;
	}

	public void setIsolationLevel(int isolationLevel) {
		this.isolationLevel = isolationLevel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public boolean isQuotes() {
		return quotes;
	}

	public void setQuotes(boolean quotes) {
		this.quotes = quotes;
	}

}