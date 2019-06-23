package com.github.jsqltool.sql.catelog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DefaultCatelogHandler implements ICatelogHandler {

	@Override
	public List<String> list(Connection connection) {
		List<String> list = new ArrayList<String>();
		try (ResultSet rset = connection.getMetaData().getCatalogs();) {
			while (rset.next())
				list.add(rset.getString(1));
		} catch (Exception e) {
		}
		return list;
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
