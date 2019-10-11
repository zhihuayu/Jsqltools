package com.github.jsqltool.result;

public class TypeInfo {

	private String typeName;
	private Integer dataType; // 对应java.sql.Types
	private Integer precision; // 精度
	/**
	指示列能否包含 Null 值。 可以为下列值之一：
		typeNoNulls (0)
		typeNullable (1)
		typeNullableUnknown (2)
	 */
	private Short nullable; //
	private Boolean caseSensitive;
	// 是否支持自增
	private Boolean autoIncrement;
	private Boolean unsignedAttribute;
	private String createParams;

	@Override
	public int hashCode() {
		if (typeName == null)
			return 0;
		return typeName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof TypeInfo) {
			TypeInfo info = (TypeInfo) obj;
			if (info.getTypeName() == this.typeName) {
				return true;
			} else if (info.getTypeName() != null && info.getTypeName().equals(this.typeName)) {
				return true;
			}
			return false;
		} else {
			return false;
		}

	}

	public String getCreateParams() {
		return createParams;
	}

	public void setCreateParams(String createParams) {
		this.createParams = createParams;
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

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Short getNullable() {
		return nullable;
	}

	public void setNullable(Short nullable) {
		this.nullable = nullable;
	}

	public Boolean getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public Boolean getAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(Boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public Boolean getUnsignedAttribute() {
		return unsignedAttribute;
	}

	public void setUnsignedAttribute(Boolean unsignedAttribute) {
		this.unsignedAttribute = unsignedAttribute;
	}

	@Override
	public String toString() {
		return typeName;
	}

}