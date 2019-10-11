package com.github.jsqltool.sql.catelog;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CatelogHandlerContent implements ICatelogHandler {

	LinkedList<ICatelogHandler> catelogs = new LinkedList<>();

	private CatelogHandlerContent() {
	}

	public static CatelogHandlerContent builder() {
		CatelogHandlerContent catelog = new CatelogHandlerContent();
		catelog.addLast(new DefaultCatelogHandler());
		return catelog;
	}

	@Override
	public List<String> list(Connection connection) {
		for (ICatelogHandler catelog : catelogs) {
			if (catelog.support(connection)) {
				return catelog.list(connection);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

	public void addLast(ICatelogHandler handler) {
		catelogs.addLast(handler);
	}

	public void addFirst(ICatelogHandler handler) {
		catelogs.addFirst(handler);
	}

}
