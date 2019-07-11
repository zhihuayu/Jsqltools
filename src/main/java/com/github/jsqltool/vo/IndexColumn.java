package com.github.jsqltool.vo;

public class IndexColumn {

	private String columnName;
	private Byte indexPosition;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Byte getIndexPosition() {
		return indexPosition;
	}

	public void setIndexPosition(Byte indexPosition) {
		this.indexPosition = indexPosition;
	}

	/**
	 *  JDBC中的索引类型
	 * @author yzh
	 * @date 2019年7月11日
	 */
	public static class IndexType {

	}

}
