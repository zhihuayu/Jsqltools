package com.github.jsqltool.param;

/**
 * 查询表数据的参数
 * @author yzh
 * @date 2019年7月27日
 */
public class SelectTableParam {
	private String catalog;
	private String schema;
	private String tableName;
	// 以下为分页参数
	private Long count;
	private Integer page;
	private Integer pageSize; // 默认值为1000

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

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

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

}
