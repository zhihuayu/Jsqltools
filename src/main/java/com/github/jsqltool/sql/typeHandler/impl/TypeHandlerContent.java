package com.github.jsqltool.sql.typeHandler.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.sql.typeHandler.TypeHandler;

@SuppressWarnings("rawtypes")
public class TypeHandlerContent implements TypeHandler {

	private LinkedList<TypeHandler> tableHandlers = new LinkedList<>();

	private TypeHandlerContent() {
	}

	public static TypeHandlerContent builder() {
		TypeHandlerContent typeHandlerContent = new TypeHandlerContent();
		typeHandlerContent.addFirst(new NumberTypeHandler());
		typeHandlerContent.addFirst(new LongTypeHandler());
		typeHandlerContent.addFirst(new IntegerTypeHandler());
		typeHandlerContent.addFirst(new DateTypeHandler());
		typeHandlerContent.addFirst(new ClobTypeHandler());
		return typeHandlerContent;
	}

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
