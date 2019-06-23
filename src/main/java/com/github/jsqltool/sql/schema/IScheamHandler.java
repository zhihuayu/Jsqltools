package com.github.jsqltool.sql.schema;

import java.sql.Connection;
import java.util.List;

public interface IScheamHandler {

	List<String> list(Connection connection, String catelog);

	boolean support(Connection connection);

}
