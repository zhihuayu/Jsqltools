package com.github.jsqltool.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 主键信息
 * 
 * @author yzh
 * @date 2019年6月27日
 */
public class Primary {

	private String primaryName;

	private List<IndexColumn> columns;

	public String getPrimaryName() {
		return primaryName;
	}

	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}

	public Boolean addColumn(IndexColumn column) {
		if (column == null) {
			return false;
		}
		if (columns == null) {
			columns = new ArrayList<>();
		}
		return columns.add(column);
	}

	public List<IndexColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<IndexColumn> columns) {
		this.columns = columns;
	}

}
