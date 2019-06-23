package com.github.jsqltool.sql.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * schema的处理类，一般来说schema的值为空，但是对于Oracle数据库来说其值就是对应的数据库用户
 * 
 * @author yzh
 *
 * @date 2019年6月17日
 */
public class DefaultSchemaHandler implements IScheamHandler {

	@Override
	public List<String> list(Connection connection, String catelog) {
		List<String> list = new ArrayList<>();
		catelog = StringUtils.isBlank(catelog) ? null : catelog.toUpperCase();
		try (ResultSet rset = connection.getMetaData().getSchemas(catelog, "%");) {
			while (rset.next())
				list.add(rset.getString(1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public boolean support(Connection connection) {
		return true;
	}

}
