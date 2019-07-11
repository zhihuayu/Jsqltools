package com.github.jsqltool.sql.index;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

/**
 * JDBC方式获取的索引和主键
 * 
 * @author yzh
 * @date 2019年6月27日
 */
public class JDBCIndexInfoHandler implements IIndexInfoHandler {

	@Override
	public List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException {
		return JdbcUtil.getIndexInfo(connect, param);
	}

	@Override
	public Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException {
		return JdbcUtil.getPrimaryInfo(connect, param);
	}

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
