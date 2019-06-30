package com.github.jsqltool.param;

import java.util.ArrayList;
import java.util.List;

public class UpdateParam {

	private String catalog;
	private String schema;
	private String tableName;
	private List<ChangeValue> values;

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

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<ChangeValue> getValues() {
		return values;
	}

	public void setValues(List<ChangeValue> values) {
		this.values = values;
	}

	public boolean addValue(ChangeValue value) {
		if (value == null) {
			return false;
		}
		if (values == null) {
			values = new ArrayList<>();
		}
		return values.add(value);
	}

}
