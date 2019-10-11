package com.github.jsqltool.sql.delete;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.UpdateResult;

public class DeleteHandlerContent implements IdeleteHandler {

	LinkedList<IdeleteHandler> deleteHandlers = new LinkedList<>();

	private DeleteHandlerContent() {
	}

	public static DeleteHandlerContent builder() {
		DeleteHandlerContent deleteHandlerContent = new DeleteHandlerContent();
		deleteHandlerContent.addLast(new DefaultDeleteHandler());
		deleteHandlerContent.addFirst(new MySqlDeleteHandler());
		deleteHandlerContent.addFirst(new OracleDeleteHandler());
		return deleteHandlerContent;
	}

	@Override
	public UpdateResult delete(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		UpdateResult result = new UpdateResult();
		result.setCode(UpdateResult.SERVER_ERROR);
		result.setMsg("找不到对应的数据库delete处理器进行处理，请添加IdeleteHandler实例！");
		for (IdeleteHandler handler : deleteHandlers) {
			if (handler.support(dbType)) {
				return handler.delete(connect, updates, force);
			}
		}
		return result;
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	public void addLast(IdeleteHandler handler) {
		deleteHandlers.addLast(handler);
	}

	public void addFirst(IdeleteHandler handler) {
		deleteHandlers.addFirst(handler);
	}

}
