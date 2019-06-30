package com.github.jsqltool.sql.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.UpdateResult;

public interface IUpdateDataHandler {

	UpdateResult update(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException;

	boolean support(DBType dbType);

}
