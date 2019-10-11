package com.github.jsqltool.param;

/**
 * 用于获取数据对象的列表信息或建表语句的参数
 * 
 * @author yzh
 * @date 2019年6月17日
 */
public class DBObjectParam {

	private String catalog;
	private String schema;
	/**
	 * 在获取对象列表信息时，该值可以为空，但是在获取建表语句时该值就不能为空
	 */
	private String name;
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String table) {
		this.name = table;
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
