package com.github.jsqltool.sql.index;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

public class IndexInfoHandlerContent implements IIndexInfoHandler {

	LinkedList<IIndexInfoHandler> indexInfo = new LinkedList<>();

	@Override
	public List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		for (IIndexInfoHandler info : indexInfo) {
			if (info.support(dbType)) {
				return info.getIndexInfo(connect, param);
			}
		}
		return null;
	}

	@Override
	public Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		DBType dbType = DBType.getDBTypeByDriverClassName(connect.getMetaData().getDriverName());
		for (IIndexInfoHandler info : indexInfo) {
			if (info.support(dbType)) {
				return info.getPrimaryInfo(connect, param);
			}
		}
		return null;
	}

	public void addLast(IIndexInfoHandler indexInfoHandler) {
		indexInfo.addLast(indexInfoHandler);
	}

	public void addFirst(IIndexInfoHandler indexInfoHandler) {
		indexInfo.addFirst(indexInfoHandler);
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
