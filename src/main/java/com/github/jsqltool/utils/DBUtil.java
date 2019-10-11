package com.github.jsqltool.utils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.result.ColumnInfo;
import com.github.jsqltool.result.SimpleTableInfo;
import com.github.jsqltool.result.TableColumnInfo;
import com.github.jsqltool.result.TypeInfo;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.IndexColumn;
import com.github.jsqltool.vo.Primary;

public class DBUtil {

	public static List<TableColumnInfo> getTableColumn(ResultSet rs) throws SQLException {
		List<TableColumnInfo> columns = new ArrayList<>();
		while (rs.next()) {
			TableColumnInfo col = new TableColumnInfo();
			col.setTableCatalog(rs.getString(TableColumnInfo.TABLE_CAT));
			col.setTableSchema(rs.getString(TableColumnInfo.TABLE_SCHEM));
			col.setTableName(rs.getString(TableColumnInfo.TABLE_NAME));
			col.setColumnName(rs.getString(TableColumnInfo.COLUMN_NAME));
			col.setDataType(rs.getInt(TableColumnInfo.DATA_TYPE));
			col.setTypeName(rs.getString(TableColumnInfo.TYPE_NAME));
			col.setColumnSize(rs.getInt(TableColumnInfo.COLUMN_SIZE));
			col.setDecimalDigits(rs.getInt(TableColumnInfo.DECIMAL_DIGITS));
			col.setRadix(rs.getInt(TableColumnInfo.NUM_PREC_RADIX));
			col.setNullable(DatabaseMetaData.columnNoNulls != rs.getInt(TableColumnInfo.NULLABLE));
			col.setRemarks(rs.getString(TableColumnInfo.REMARKS));
			col.setColumnDefaultValue(rs.getString(TableColumnInfo.COLUMN_DEF));
			col.setOrdinal(rs.getInt(TableColumnInfo.ORDINAL_POSITION));
			col.setColumnDefaultValue(rs.getString(TableColumnInfo.COLUMN_DEF));
			columns.add(col);
		}
		return columns;
	}

	public static List<ColumnInfo> getColumnInfo(ResultSet resultSet) throws SQLException {
		List<ColumnInfo> infos = new ArrayList<>();
		while (resultSet.next()) {
			ColumnInfo info = new ColumnInfo();
			info.setColumnName(resultSet.getString("COLUMN_NAME"));
			info.setColumnSize(resultSet.getInt("COLUMN_SIZE"));
			info.setDecimalDigits(resultSet.getInt("DECIMAL_DIGITS"));
			info.setTypeName(resultSet.getString("TYPE_NAME"));
			info.setDataType(resultSet.getInt("DATA_TYPE"));
			info.setNullable(resultSet.getInt("NULLABLE"));
			info.setRemarks(resultSet.getString("REMARKS"));
			info.setIsNullable(resultSet.getString("IS_NULLABLE"));
			info.setIsAutoincrement(resultSet.getString("IS_AUTOINCREMENT"));
			infos.add(info);
		}
		return infos;
	}

	public static List<SimpleTableInfo> getTableInfo(ResultSet rset) throws SQLException {
		List<SimpleTableInfo> list = new ArrayList<>();
		while (rset.next()) {
			SimpleTableInfo info = new SimpleTableInfo();
			info.setTableName(rset.getString(3));
			info.setComment(rset.getString(5));
			list.add(info);
		}
		return list;
	}

	public static Set<TypeInfo> getTypeInfo(ResultSet resultSet) throws SQLException {
		Set<TypeInfo> types = new LinkedHashSet<>();
		while (resultSet.next()) {
			TypeInfo info = new TypeInfo();
			info.setTypeName(resultSet.getString("TYPE_NAME"));
			info.setDataType(resultSet.getInt("DATA_TYPE"));
			info.setPrecision(resultSet.getInt("PRECISION"));
			info.setNullable(resultSet.getShort("NULLABLE"));
			info.setCaseSensitive(resultSet.getBoolean("CASE_SENSITIVE"));
			info.setAutoIncrement(resultSet.getBoolean("AUTO_INCREMENT"));
			info.setUnsignedAttribute(resultSet.getBoolean("UNSIGNED_ATTRIBUTE"));
			info.setCreateParams(resultSet.getString("CREATE_PARAMS"));
			types.add(info);
		}
		return types;
	}

	public static List<Index> getIndexInfo(ResultSet resultSet) throws SQLException {
		List<Index> result = new ArrayList<>();
		while (resultSet.next()) {
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
		return result;
	}
	
	public static Primary getPrimaryInfo(ResultSet resultSet) throws SQLException {
		Primary primary = null;
		while (resultSet.next()) {
			if (StringUtils.isNotBlank(resultSet.getString("COLUMN_NAME"))) {
				if (primary == null) {
					primary = new Primary();
					primary.setPrimaryName(resultSet.getString("PK_NAME"));
				}
				IndexColumn indexColumn = new IndexColumn();
				indexColumn.setColumnName(resultSet.getString("COLUMN_NAME"));
				indexColumn.setIndexPosition(resultSet.getByte("KEY_SEQ"));
				primary.addColumn(indexColumn);
			}
		}
		return primary;
	}

}
