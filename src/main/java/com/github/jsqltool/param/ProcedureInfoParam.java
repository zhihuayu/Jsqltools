package com.github.jsqltool.param;

/**
 * 用于获取存储过程信息的参数
 * @author yzh
 * @date 2019年8月11日
 */
public class ProcedureInfoParam {

	private String catalog;
	private String schema;
	private String procedureName;

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

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

}