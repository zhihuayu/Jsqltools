package com.github.jsqltool.sql.dropTable;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.DropTableParam;
import com.github.jsqltool.vo.UpdateResult;

/**
 * 表删除的基础接口
 * @author yzh
 * @date 2019年7月9日
 */
public interface IdropTableHandler {

	UpdateResult drop(Connection connect, DropTableParam dropTableParam) throws SQLException;

	boolean support(DBType dbType);

}
