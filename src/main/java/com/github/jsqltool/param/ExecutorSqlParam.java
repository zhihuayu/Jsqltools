package com.github.jsqltool.param;

/**
 * 执行SQL时的参数
 * 
 * @author yzh
 * @date 2019年6月22日
 */
public class ExecutorSqlParam {
	private String catalog;
	private String schema;
	private String sql;
	// 以下为分页参数
	private Long count;
	private Boolean isCount; // 是否查询总数
	private Integer page;
	private Integer pageSize; // 默认值为1000

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

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Boolean getIsCount() {
		return isCount;
	}

	public void setIsCount(Boolean isCount) {
		this.isCount = isCount;
	}

}
