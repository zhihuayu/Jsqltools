package com.github.jsqltool.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 索引信息
 * 
 * @author yzh
 * @date 2019年6月27日
 */
public class Index {

	private String indexName;
	private Boolean NonUnique;

	private List<IndexColumn> columns;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Boolean getNonUnique() {
		return NonUnique;
	}

	public void setNonUnique(Boolean nonUnique) {
		NonUnique = nonUnique;
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
