package com.github.jsqltool.sql.alter;

import java.sql.Connection;

public interface IAlterStatement {

//	UpdateResult alterTable(Connection connect, TableInfoParam info);

	boolean support(Connection connection);

//	class TableInfoParam {
//
//		private String catelog;
//		private String schema;
//		private String tableName;
//
//	}

}
