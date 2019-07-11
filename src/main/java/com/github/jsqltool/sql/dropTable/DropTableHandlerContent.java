package com.github.jsqltool.sql.dropTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.param.DropTableParam;
import com.github.jsqltool.vo.UpdateResult;

public class DropTableHandlerContent implements IdropTableHandler {

	LinkedList<IdropTableHandler> dropTableHandlers = new LinkedList<>();

	@Override
	public UpdateResult drop(Connection connection, DropTableParam dropTableParam) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connection.getMetaData().getDriverName());
		for (IdropTableHandler schema : dropTableHandlers) {
			if (schema.support(dbType)) {
				return schema.drop(connection, dropTableParam);
			}
		}
		throw new JsqltoolParamException("没有找到对应的IdropTableHanlder来处理相应的drop语句！");
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(IdropTableHandler handler) {
		dropTableHandlers.addLast(handler);
	}

	public void addFirst(IdropTableHandler handler) {
		dropTableHandlers.addFirst(handler);
	}

}
