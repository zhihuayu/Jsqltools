package com.github.jsqltool.param;

/**
 * 用于获取table列表信息的参数
 * 
 * @author yzh
 * @date 2019年6月17日
 */
public class TablesParam {

	private String catelog;
	private String schema;
	private String type;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
