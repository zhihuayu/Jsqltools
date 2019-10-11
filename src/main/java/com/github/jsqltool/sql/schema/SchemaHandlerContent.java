package com.github.jsqltool.sql.schema;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SchemaHandlerContent implements IScheamHandler {

	LinkedList<IScheamHandler> schemas = new LinkedList<>();

	private SchemaHandlerContent() {
	}

	public static SchemaHandlerContent builder() {
		SchemaHandlerContent schema = new SchemaHandlerContent();
		schema.addLast(new DefaultSchemaHandler());
		return schema;
	}

	@Override
	public List<String> list(Connection connection, String catelog) {
		for (IScheamHandler schema : schemas) {
			if (schema.support(connection)) {
				return schema.list(connection, catelog);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

	public void addLast(IScheamHandler handler) {
		schemas.addLast(handler);
	}

	public void addFirst(IScheamHandler handler) {
		schemas.addFirst(handler);
	}

}
