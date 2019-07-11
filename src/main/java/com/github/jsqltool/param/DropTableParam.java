package com.github.jsqltool.param;

import java.util.List;

/**
 * 删除指定类型的对象的参数（表或者视图）
 * @author yzh
 * @date 2019年7月9日
 */
public class DropTableParam {

	private String catalog;
	private String schema;

	List<SimpleInfo> infos;

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

	public List<SimpleInfo> getInfos() {
		return infos;
	}

	public void setInfos(List<SimpleInfo> infos) {
		this.infos = infos;
	}

	public static class SimpleInfo {
		private String tableName;
		private String type; // 类型，如：TAbLE、VIEW等

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

}
