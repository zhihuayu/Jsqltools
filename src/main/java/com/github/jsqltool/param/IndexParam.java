package com.github.jsqltool.param;

/**
 * 获取索引的参数
 * 
 * @author yzh
 *
 * @date 2019年6月27日
 */
public class IndexParam {

	private String catalog;
	private String schema;
	private String table;
	// 是否是unique，如果为false则代表获取所有的索引不管其是不是unique索引，如果获取的是主键该值可以不进行设置
	private Boolean unique = false;

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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Boolean getUnique() {
		return unique;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

}
