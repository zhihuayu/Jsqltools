package com.github.jsqltool.sql.insert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.UpdateResult;

/**
 * 表插入操作的基本类 
 * @author yzh
 * @date 2019年7月6日
 */
public interface IinertHandler {

	UpdateResult insert(Connection connect, List<UpdateParam> updates) throws SQLException;

	boolean support(DBType dbType);
}
