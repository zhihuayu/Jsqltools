package com.github.jsqltool.sql.alter;

import java.sql.Connection;

public interface IAlterStatement {

	
	
	boolean support(Connection connection);
	
}
