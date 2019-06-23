package com.github.jsqltool.sql.tableColumn;

import java.sql.Connection;
import java.util.List;

import com.github.jsqltool.param.TableColumnsParam;
import com.github.jsqltool.sql.TableColumnInfo;

public interface ITableColumnHandler {

	List<TableColumnInfo> list(Connection connection, TableColumnsParam param);

	boolean support(Connection connection);

}
