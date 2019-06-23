package com.github.jsqltool.param;

import org.apache.commons.lang3.StringUtils;

public class CreateParam {

	private String type;
	private String columnName;
	private Integer length;
	private Integer decimals;
	private Boolean notNull;
	private String defaultValue;
	private String comment;
	private Boolean primaryKey;
	private Boolean autoIncrement;

	/**
	 * 简单的校验
	 * 
	 * @author yzh
	 *
	 * @date 2019年6月20日
	 */
	public static boolean validate(CreateParam param) {
		if (StringUtils.isBlank(param.getColumnName()) || StringUtils.isBlank(param.getType())) {
			return false;
		}
		return true;
	}

	public Boolean getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDecimals() {
		return decimals;
	}

	public void setDecimals(Integer decimals) {
		this.decimals = decimals;
	}

	public Boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(Boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

}
