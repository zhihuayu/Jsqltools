package com.github.jsqltool.param;

/**
 * 用于获取table列表信息或建表语句的参数
 * 
 * @author yzh
 * @date 2019年6月17日
 */
public class TablesParam {

	private String catalog;
	private String schema;
	/**
	 * 在获取table列表信息时，该值可以为空，但是在获取建表语句时该值就不能为空
	 */
	private String table;
	private String type;

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
