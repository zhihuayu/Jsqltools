package com.github.jsqltool.result;

import java.sql.Types;
import java.util.List;

public class SqlResult {
	public static final int success = 200;
	public static final int fail = 500;

	private int status;
	private String message;
	private List<Column> columns;
	private List<Record> records;
	private long count; // 总数
	private long page; // 当前页
	private long pageSize; // 每页大小

	public SqlResult() {
	}

	public SqlResult(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getPage() {
		return page;
	}

	public void setPage(long page) {
		this.page = page;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

	public boolean success() {
		return (this.status == success);
	}

	public static SqlResult success(String message) {
		return new SqlResult(success, message);
	}

	public static SqlResult error(String message) {
		return new SqlResult(fail, message);
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Column> getColumns() {
		return this.columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public List<Record> getRecords() {
		return this.records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public static class Column {
		private String alias;
		private String columnName;
		/**
		 * {@link Types}类型
		 */
		private Integer dataType;
		private String typeName;
		private Boolean autoIncrement;

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public Integer getDataType() {
			return dataType;
		}

		public void setDataType(Integer dataType) {
			this.dataType = dataType;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public Boolean getAutoIncrement() {
			return autoIncrement;
		}

		public void setAutoIncrement(Boolean autoIncrement) {
			this.autoIncrement = autoIncrement;
		}

	}

	public static class Record {

		private List<Object> values;

		public Record() {
		}

		public Record(List<Object> values) {
			this.values = values;
		}

		public List<Object> getValues() {
			return this.values;
		}

		public void setValues(List<Object> values) {
			this.values = values;
		}

	}

}