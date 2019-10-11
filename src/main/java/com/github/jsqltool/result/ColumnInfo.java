package com.github.jsqltool.result;

public class ColumnInfo {

	private String columnName;
	private Integer columnSize;
	/**
	 * the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable. 
	 */
	private Integer decimalDigits;

	/**
	 * Data source dependent type name, for a UDT the type name is fully qualified 
	 */
	private String typeName;
	/**
	 * SQL type from java.sql.Types 
	 */
	private Integer dataType;

	/**
	 *  ◦ columnNoNulls(0) - might not allow NULL values 
	 *  ◦ columnNullable(1) - definitely allows NULL values 
	 *  ◦ columnNullableUnknown(2) - nullability unknown 
	 */
	private Integer nullable;
	private String remarks;
	/**
	 *  ISO rules are used to determine the nullability for a column. 
	 *  ◦ YES --- if the column can include NULLs 
	 *  ◦ NO --- if the column cannot include NULLs 
	 *  ◦ empty string --- if the nullability for the column is unknown 
	 */
	private String isNullable;
	/**
	 *  Indicates whether this column is auto incremented
	 *   ◦ YES --- if the column is auto incremented 
	 *   ◦ NO --- if the column is not auto incremented 
	 *   ◦ empty string --- if it cannot be determined whether the column is auto incremented 
	 */
	private String isAutoincrement;

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Integer getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getDataType() {
		return dataType;
	}

	public void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

	public Integer getNullable() {
		return nullable;
	}

	public void setNullable(Integer nullable) {
		this.nullable = nullable;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getIsNullable() {
		return isNullable;
	}

	public void setIsNullable(String isNullable) {
		this.isNullable = isNullable;
	}

	public String getIsAutoincrement() {
		return isAutoincrement;
	}

	public void setIsAutoincrement(String isAutoincrement) {
		this.isAutoincrement = isAutoincrement;
	}

	@Override
	public String toString() {
		return typeName;
	}

}