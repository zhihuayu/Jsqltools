package com.github.jsqltool.sql.create;

import java.sql.Connection;
import java.util.List;

import com.github.jsqltool.param.CreateParam;
import com.github.jsqltool.param.TableColumnsParam;

public interface ICreateStatement {

	List<String> createTable(Connection connection, TableColumnsParam tableParam, List<CreateParam> param);

	boolean support(Connection connection);

}
