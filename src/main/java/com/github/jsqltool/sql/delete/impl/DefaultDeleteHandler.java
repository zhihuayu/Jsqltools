package com.github.jsqltool.sql.delete.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.UpdateDataException;
import com.github.jsqltool.param.ChangeValue;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.delete.AbstractDeleteHandler;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.vo.IndexColumn;
import com.github.jsqltool.vo.Primary;
import com.github.jsqltool.vo.UpdateResult;

/**
 *默认的数据库数据修改处理器 
 * @author yzh
 * @date 2019年6月30日
 */
public class DefaultDeleteHandler extends AbstractDeleteHandler {

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

	@Override
	protected UpdateResult beforeGeneratorSql(Primary primayInfo, UpdateParam param, UpdateResult updateResult) {
		if (primayInfo == null) {
			updateResult.setCode(UpdateResult.WARN);
			String msg = updateResult.getMsg();
			if (msg == null)
				msg = "";
			msg = msg + "<br>表：" + param.getTableName() + "没有主键，会修改所有数据";
			updateResult.setMsg(msg);
		}
		return updateResult;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected String getSqlWhere(List<Object> zwf, List<ChangeValue> values, Primary primayInfo) {
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		TypeHandler typeHandler = builder.getTypeHandler();
		StringBuilder sb = new StringBuilder();
		sb.append(" where ");
		// 拼装where条件
		if (primayInfo != null) {
			List<IndexColumn> columns = primayInfo.getColumns();
			for (IndexColumn index : columns) {
				boolean isFind = false;
				for (ChangeValue ch : values) {
					// 判断如果列相等
					if (StringUtils.equalsIgnoreCase(ch.getColumnName(), index.getColumnName())) {
						isFind = true;
						sb.append(ch.getColumnName());
						sb.append("=? and ");
						zwf.add(typeHandler.getParam(ch.getOldValue(), JdbcType.forCode(ch.getDataType())));
						break;
					}
					if (!isFind) {
						throw new UpdateDataException("找不到对应的索引行：" + index.getColumnName());
					}
				}
			}
			sb.setLength(sb.length() - 4);
		} else {
			// 没有主键则拼装所有的值
			for (ChangeValue ch : values) {
				sb.append(ch.getColumnName());
				if (ch.getOldValue() == null) {
					sb.append(" is null");
				} else {
					sb.append(" = ?");
					zwf.add(typeHandler.getParam(ch.getOldValue(), JdbcType.forCode(ch.getDataType())));
				}
				sb.append(" and ");
			}
			sb.setLength(sb.length() - 4);
		}
		return sb.toString();
	}

}
