package com.github.jsqltool.sql.index;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.vo.Index;
import com.github.jsqltool.vo.Primary;

/**
 * 获取主键和索引的基础接口
 * 
 * @author yzh
 * @date 2019年6月27日
 */
public interface IIndexInfoHandler {

	List<Index> getIndexInfo(Connection connect, IndexParam param) throws SQLException;

	Primary getPrimaryInfo(Connection connect, IndexParam param) throws SQLException;

	boolean support(DBType dbType);

}
