package com.github.jsqltool.utils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.jsqltool.sql.TableColumnInfo;

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

}
