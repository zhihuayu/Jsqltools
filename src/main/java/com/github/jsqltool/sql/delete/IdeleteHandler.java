package com.github.jsqltool.sql.delete;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.UpdateResult;

/**
 * 表数据删除处理基类
 * @author yzh
 * @date 2019年7月6日
 */
public interface IdeleteHandler {

	UpdateResult delete(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException;

	boolean support(DBType dbType);
}
