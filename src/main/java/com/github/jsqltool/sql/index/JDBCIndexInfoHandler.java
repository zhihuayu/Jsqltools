package com.github.jsqltool.sql.index;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.IndexColumn;
import com.github.jsqltool.vo.Primary;

/**
 * JDBC方式获取的索引和主键
 * 
 * @author yzh
 * @date 2019年6月27日
 */
public class JDBCIndexInfoHandler implements IIndexInfoHandler {

	@Override
	public List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		DatabaseMetaData metaData = connect.getMetaData();
		boolean storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
		boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
		String catelog = null;
		String schema = null;
		String table = null;
		if (storesUpperCaseIdentifiers) {
			catelog = StringUtils.upperCase(param.getCatalog());
			schema = StringUtils.upperCase(param.getSchema());
			table = StringUtils.upperCase(param.getTable());
		} else if (storesLowerCaseIdentifiers) {
			catelog = StringUtils.lowerCase(param.getCatalog());
			schema = StringUtils.lowerCase(param.getSchema());
			table = StringUtils.lowerCase(param.getTable());
		} else {
			catelog = param.getCatalog();
			schema = param.getSchema();
			table = param.getTable();
		}

		Boolean unique = param.getUnique();
		List<Index> result = new ArrayList<>();
		try (ResultSet resultSet = metaData.getIndexInfo(catelog, schema, table, unique, false);) {
			while (resultSet.next()) {
				addIndex(resultSet, result);
			}
		}
		return result;
	}

	private void addIndex(ResultSet resultSet, List<Index> result) throws SQLException {
		if (StringUtils.isNotBlank(resultSet.getString("INDEX_NAME"))) {
			String indexName = resultSet.getString("INDEX_NAME");
			Index index = null;
			for (Index id : result) {
				if (id.getIndexName().equals(indexName)) {
					index = id;
					break;
				}
			}
			if (index == null) {
				index = new Index();
				index.setIndexName(indexName);
				index.setNonUnique(resultSet.getBoolean("NON_UNIQUE"));
				result.add(index);
			}
			IndexColumn indexColumn = new IndexColumn();
			indexColumn.setColumnName(resultSet.getString("COLUMN_NAME"));
			indexColumn.setIndexPosition(resultSet.getByte("ORDINAL_POSITION"));
			index.addColumn(indexColumn);
		}
	}

	@Override
	public Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		DatabaseMetaData metaData = connect.getMetaData();
		boolean storesUpperCaseIdentifiers = metaData.storesUpperCaseIdentifiers();
		boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();
		String catalog = null;
		String schema = null;
		String table = null;
		if (storesUpperCaseIdentifiers) {
			catalog = StringUtils.upperCase(param.getCatalog());
			schema = StringUtils.upperCase(param.getSchema());
			table = StringUtils.upperCase(param.getTable());
		} else if (storesLowerCaseIdentifiers) {
			catalog = StringUtils.lowerCase(param.getCatalog());
			schema = StringUtils.lowerCase(param.getSchema());
			table = StringUtils.lowerCase(param.getTable());
		} else {
			catalog = param.getCatalog();
			schema = param.getSchema();
			table = param.getTable();
		}
		Primary primary = null;
		try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schema, table);) {
			while (primaryKeys.next()) {
				primary = getPrimaryKey(primaryKeys, primary);
			}
		}
		return primary;
	}

	private Primary getPrimaryKey(ResultSet primaryKeys, Primary primary) throws SQLException {
		if (StringUtils.isNotBlank(primaryKeys.getString("COLUMN_NAME"))) {
			if (primary == null) {
				primary = new Primary();
				primary.setPrimaryName(primaryKeys.getString("PK_NAME"));
			}
			IndexColumn indexColumn = new IndexColumn();
			indexColumn.setColumnName(primaryKeys.getString("COLUMN_NAME"));
			indexColumn.setIndexPosition(primaryKeys.getByte("KEY_SEQ"));
			primary.addColumn(indexColumn);
		}
		return primary;
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
