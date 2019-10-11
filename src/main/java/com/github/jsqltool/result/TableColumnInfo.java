package com.github.jsqltool.result;

import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表的列信息 
 * @author yzh
 * @date 2019年8月10日
 */
public class TableColumnInfo {

	public final static int TABLE_CAT = 1;
	public final static int TABLE_SCHEM = 2;
	public final static int TABLE_NAME = 3;
	public final static int COLUMN_NAME = 4;
	public final static int DATA_TYPE = 5;
	public final static int TYPE_NAME = 6;
	public final static int COLUMN_SIZE = 7;
	public final static int BUFFER_LENGTH = 8;
	public final static int DECIMAL_DIGITS = 9;
	public final static int NUM_PREC_RADIX = 10;
	public final static int NULLABLE = 11;
	public final static int REMARKS = 12;
	public final static int COLUMN_DEF = 13;
	public final static int SQL_DATA_TYPE = 14; // unused
	public final static int SQL_DATETIME_SUB = 15;// unused
	public final static int CHAR_OCTET_LENGTH = 16;
	public final static int ORDINAL_POSITION = 17;
	public final static int IS_NULLABLE = 18;
	public final static int IS_AUTOINCREMENT = 23;

	protected String tableCatalog = null;

	protected String tableSchema = null;

	protected String tableName = null;

	protected String columnName = null;

	protected int dataType = 0;

	protected String typeName = null;

	protected int columnSize = 0;

	protected int decimalDigits = 0;

	protected int radix = 0;

	protected boolean nullable = true;

	protected String remarks = null;

	protected String columnDefaultValue = null;

	protected int ordinal = -1;

	protected boolean pkComponent = false;

	protected boolean isForeignKey = false;

	public void setTableCatalog(String tableCatalog) {
		this.tableCatalog = tableCatalog;
	}

	public void setTableSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public void setRadix(int radix) {
		this.radix = radix;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setColumnDefaultValue(String columnDefaultValue) {
		this.columnDefaultValue = columnDefaultValue;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public void setPkComponent(boolean pkComponent) {
		this.pkComponent = pkComponent;
	}

	public void setIsForeignKey(boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}

	// getters

	public String getTableCatalog() {
		return tableCatalog;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getDataType() {
		return dataType;
	}

	public String getTypeName() {
		return typeName;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public int getDecimalDigits() {
		return decimalDigits;
	}

	public int getRadix() {
		return radix;
	}

	public boolean getNullable() {
		return nullable;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getColumnDefaultValue() {
		return columnDefaultValue;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public boolean getPkComponent() {
		return pkComponent;
	}

	public boolean getIsForeignKey() {
		return isForeignKey;
	}

	/**
	 * Returns the columnName enclosed in the provided "quote" characters, if the
	 * columnName contains a space or a hyphen, otherwise returns the columnName
	 * as-is. Normally, the openQuote and closeQuote args will both be regular
	 * double quotation marks ("), however, Microsoft databases use square brackets
	 * ([ and ]).
	 */
	public String maybeQuoteColumnName(char openQuote, char closeQuote) {
		if (columnName == null)
			return null;
		if (columnName.trim().length() == 0)
			return "";
		if (columnName.indexOf(' ') < 0 && columnName.indexOf('-') < 0)
			return columnName;
		StringBuffer buffer = new StringBuffer(columnName.length() + 4);
		return buffer.append(openQuote).append(columnName).append(closeQuote).toString();
	}

	final static Map<Integer, String> typeStrings = new ConcurrentHashMap<Integer, String>();
	static {
		typeStrings.put(Types.ARRAY, "ARRAY");
		typeStrings.put(Types.BIGINT, "BIGINT");
		typeStrings.put(Types.BINARY, "BINARY");
		typeStrings.put(Types.BIT, "BIT");
		typeStrings.put(Types.BLOB, "BLOB");
		typeStrings.put(Types.CHAR, "CHAR");
		typeStrings.put(Types.CLOB, "CLOB");
		typeStrings.put(Types.DATE, "DATE");
		typeStrings.put(Types.DECIMAL, "DECIMAL");
		typeStrings.put(Types.DISTINCT, "DISTINCT");
		typeStrings.put(Types.DOUBLE, "DOUBLE");
		typeStrings.put(Types.FLOAT, "FLOAT");
		typeStrings.put(Types.INTEGER, "INTEGER");
		typeStrings.put(Types.JAVA_OBJECT, "JAVA_OBJECT");
		typeStrings.put(Types.LONGVARBINARY, "LONGVARBINARY");
		typeStrings.put(Types.LONGVARCHAR, "LONGVARCHAR");
		typeStrings.put(Types.NULL, "NULL");
		typeStrings.put(Types.NUMERIC, "NUMERIC");
		typeStrings.put(Types.OTHER, "OTHER");
		typeStrings.put(Types.REAL, "REAL");
		typeStrings.put(Types.REF, "REF");
		typeStrings.put(Types.SMALLINT, "SMALLINT");
		typeStrings.put(Types.STRUCT, "STRUCT");
		typeStrings.put(Types.TIME, "TIME");
		typeStrings.put(Types.TIMESTAMP, "TIMESTAMP");
		typeStrings.put(Types.TINYINT, "TINYINT");
		typeStrings.put(Types.VARBINARY, "VARBINARY");
		typeStrings.put(Types.VARCHAR, "VARCHAR");
	}

}