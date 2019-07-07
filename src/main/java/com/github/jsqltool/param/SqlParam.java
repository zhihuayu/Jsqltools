package com.github.jsqltool.param;

/**
 * 代表一条SQL语句，一般为预编译语句 {@code SqlPlus}的excutePrepareStatement方法
 * 
 * @author yzh
 * @date 2019年6月28日
 */
public class SqlParam {

	private Integer status;
	private String msg;
	private String catalog;
	private String schema;
	private String sql;
	private Object[] param;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParam() {
		return param;
	}

	public void setParam(Object[] param) {
		this.param = param;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

}