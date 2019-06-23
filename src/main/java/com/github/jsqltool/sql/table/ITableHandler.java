package com.github.jsqltool.sql.table;

import java.sql.Connection;
import java.util.List;

import com.github.jsqltool.param.TablesParam;
import com.github.jsqltool.sql.SimpleTableInfo;

public interface ITableHandler {
	List<SimpleTableInfo> list(Connection connection, TablesParam param);

	boolean support(Connection connection);
}
