package com.github.jsqltool.sql.typeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.JdbcType;

@SuppressWarnings("rawtypes")
public class TypeHandlerContent implements TypeHandler {

	private LinkedList<TypeHandler> tableHandlers = new LinkedList<>();

	@Override
	public Object handler(ResultSet resultSet, int index, JdbcType type) throws SQLException {
		for (TypeHandler handler : tableHandlers) {
			if (handler.support(type)) {
				return handler.handler(resultSet, index, type);
			}
		}
		return resultSet.getObject(index);
	}

	public void addLast(TypeHandler typeHandler) {
		tableHandlers.addLast(typeHandler);
	}

	public void addFirst(TypeHandler typeHandler) {
		tableHandlers.addFirst(typeHandler);
	}

	@Override
	public boolean support(JdbcType type) {
		return true;
	}

	@Override
	public Object getParam(Object obj, JdbcType type) {
		for (TypeHandler handler : tableHandlers) {
			if (handler.support(type)) {
				return handler.getParam(obj, type);
			}
		}
		return obj;
	}

}
