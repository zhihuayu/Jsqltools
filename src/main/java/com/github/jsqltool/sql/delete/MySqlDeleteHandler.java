package com.github.jsqltool.sql.delete;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.vo.Primary;
import com.github.jsqltool.vo.UpdateResult;

public class MySqlDeleteHandler extends DefaultDeleteHandler {

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.MYSQL_TYPE;
	}

	@Override
	protected UpdateResult beforeGeneratorSql(Primary primayInfo, UpdateParam param, UpdateResult updateResult) {
		if (primayInfo == null) {
			updateResult.setCode(UpdateResult.WARN);
			String msg = updateResult.getMsg();
			if (msg == null)
				msg = "";
			msg = msg + "<br>表：" + param.getTableName() + "没有主键，只会删除第一条找到的记录";
			updateResult.setMsg(msg);
		}
		return updateResult;
	}

	@Override
	protected void afterSqlGenerator(StringBuilder sb) {
		sb.append("  limit  1");
	}

}
