package com.github.jsqltool.param;

/**
 * 用于获取table信息的参数
 * 
 * @author yzh
 * @date 2019年6月17日
 */
public class TableColumnsParam {

	private String catelog;
	private String schema;
	private String tableName;
	private String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCatelog() {
		return catelog;
	}

	public void setCatelog(String catelog) {
		this.catelog = catelog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
