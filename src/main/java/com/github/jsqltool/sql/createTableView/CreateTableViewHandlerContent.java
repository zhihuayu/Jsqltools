package com.github.jsqltool.sql.createTableView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.exception.CreateTableViewException;
import com.github.jsqltool.param.TablesParam;

public class CreateTableViewHandlerContent implements IcreateTableViewHandler {

	LinkedList<IcreateTableViewHandler> deleteHandlers = new LinkedList<>();

	@Override
	public String getCreateTableView(Connection connect, TablesParam param) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		for (IcreateTableViewHandler handler : deleteHandlers) {
			if (handler.support(dbType)) {
				return handler.getCreateTableView(connect, param);
			}
		}
		throw new CreateTableViewException("没有寻找对应" + dbType + "的IcreateTableViewHandler，请自行实现！");
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(IcreateTableViewHandler handler) {
		deleteHandlers.addLast(handler);
	}

	public void addFirst(IcreateTableViewHandler handler) {
		deleteHandlers.addFirst(handler);
	}

}
