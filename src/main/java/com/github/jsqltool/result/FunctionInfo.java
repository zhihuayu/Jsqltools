package com.github.jsqltool.result;

public class FunctionInfo {
	
	private String catalog;
	private String schema;
	private String name;
	private String remark;
	private Boolean hasReturnValue;//是否有返回值，如果为null，则代表未知，true代表有返回值，false代表无返回值
	private String specificName; // 在schema中的唯一标识

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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Boolean getHasReturnValue() {
		return hasReturnValue;
	}
	public void setHasReturnValue(Boolean hasReturnValue) {
		this.hasReturnValue = hasReturnValue;
	}
	public String getSpecificName() {
		return specificName;
	}
	public void setSpecificName(String specificName) {
		this.specificName = specificName;
	}
	

	

}
