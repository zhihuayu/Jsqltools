package com.github.jsqltool.sql.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.UpdateResult;

public class UpdateDataHandlerContent implements IUpdateDataHandler {

	LinkedList<IUpdateDataHandler> updateDataHandler = new LinkedList<>();

	@Override
	public UpdateResult update(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		UpdateResult result = new UpdateResult();
		result.setCode(UpdateResult.SERVER_ERROR);
		result.setMsg("找不到对应的数据库update处理器进行处理，请添加IUpdateDataHandler实例！");
		for (IUpdateDataHandler handler : updateDataHandler) {
			if (handler.support(dbType)) {
				return handler.update(connect, updates, force);
			}
		}
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(IUpdateDataHandler handler) {
		updateDataHandler.addLast(handler);
	}

	public void addFirst(IUpdateDataHandler handler) {
		updateDataHandler.addFirst(handler);
	}

}
