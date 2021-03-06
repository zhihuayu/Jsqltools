package com.github.jsqltool.sql.insert.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.insert.InsertHandler;
import com.github.jsqltool.vo.UpdateResult;

public class InsertHandlerContent implements InsertHandler {
	LinkedList<InsertHandler> insertHandlerContent = new LinkedList<>();

	private InsertHandlerContent() {
	}

	public static InsertHandlerContent builder() {
		InsertHandlerContent insertHandlerContent = new InsertHandlerContent();
		insertHandlerContent.addFirst(new DefaultInsertHandler());
		return insertHandlerContent;
	}

	@Override
	public UpdateResult insert(Connection connect, List<UpdateParam> updates) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		UpdateResult result = new UpdateResult();
		result.setCode(UpdateResult.SERVER_ERROR);
		result.setMsg("找不到对应的数据库insert处理器进行处理，请添加IinertHandler实例！");
		for (InsertHandler handler : insertHandlerContent) {
			if (handler.support(dbType)) {
				return handler.insert(connect, updates);
			}
		}
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(InsertHandler handler) {
		insertHandlerContent.addLast(handler);
	}

	public void addFirst(InsertHandler handler) {
		insertHandlerContent.addFirst(handler);
	}

}
