package com.github.jsqltool.sql.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.UpdateDataException;
import com.github.jsqltool.param.ChangeValue;
import com.github.jsqltool.param.IndexParam;
import com.github.jsqltool.param.SqlParam;
import com.github.jsqltool.param.UpdateParam;
import com.github.jsqltool.sql.typeHandler.TypeHandler;
import com.github.jsqltool.utils.JdbcUtil;
import com.github.jsqltool.vo.IndexColumn;
import com.github.jsqltool.vo.Primary;
import com.github.jsqltool.vo.UpdateResult;

/**
 * @author yzh
 *
 * @date 2019年6月30日
 */
/**
 * @author yzh
 *
 * @date 2019年6月30日
 */
public class UpdateDataHandler {

	public UpdateResult update(Connection connect, List<UpdateParam> updates, Boolean force) throws SQLException {
		UpdateResult updateResult = new UpdateResult();
		long startTime = System.currentTimeMillis();
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		if (updates == null || updates.isEmpty()) {
			throw new UpdateDataException("更新的数据为空");
		}
		Set<String> tables = new HashSet<>();
		for (UpdateParam u : updates) {
			tables.add(u.getTableName());
		}
		Map<String, List<SqlParam>> sqls = new HashMap<>();
		// 遍历表名
		for (String tableName : tables) {
			UpdateParam update = getByTableName(updates, tableName);
			IndexParam indexParam = new IndexParam();
			indexParam.setCatalog(update.getCatalog());
			indexParam.setSchema(update.getSchema());
			indexParam.setTable(update.getTableName());
			indexParam.setUnique(true);
			Primary primayInfo = builder.getPrimayInfo(connect, indexParam);
			boolean isFirst = true;
			for (UpdateParam param : updates) {
				if (StringUtils.equals(tableName, param.getTableName())) {
					if (isFirst) {
						updateResult = beforeGeneratorSql(primayInfo, param, updateResult);
						isFirst = false;
					}
					SqlParam sqlparam = getSqlParam(connect, param, primayInfo);
					List<SqlParam> list = sqls.get(tableName);
					if (list == null)
						list = new ArrayList<>();
					list.add(sqlparam);
					sqls.put(tableName, list);
				}
			}
		}
		// 如果没有强制执行，则遇到警告就直接返回不执行
		if (updateResult.getCode() != null && updateResult.getCode().equals(UpdateResult.WARN) && !force) {
			return updateResult;
		}
		Set<Entry<String, List<SqlParam>>> entrySet = sqls.entrySet();
		for (Entry<String, List<SqlParam>> entry : entrySet) {
			// String key = entry.getKey();
			List<SqlParam> value = entry.getValue();
			for (SqlParam sql : value) {
				PreparedStatement prepareStatement = connect.prepareStatement(sql.getSql());
				Object[] param = sql.getParam();
				for (int i = 0; i < param.length; i++)
					prepareStatement.setObject(i + 1, param[i]);
				int executeUpdate = prepareStatement.executeUpdate();
				System.out.println(sql.getSql() + "影响的行数：" + executeUpdate);
			}
			JdbcUtil.commit(connect);
		}
		long endTime = System.currentTimeMillis();
		updateResult.setCode(UpdateResult.OK);
		updateResult.setMsg("执行成功");
		updateResult.setTime(endTime - startTime);
		return updateResult;
	}

	/**
	 * 
	* @author yzh
	* @date 2019年6月30日
	* @Description:  在生成SQL语句之前该方法会被执行，用以监测参数的设置情况，
	*     并提前设置UpdateResult，对于每一个table该方法只会执行一次
	*  @param dbType  数据类型
	*  @param primayInfo
	*  @param param
	*  @param updateResult
	*  @return    参数
	* @return UpdateResult    返回类型
	* @throws
	 */
	private UpdateResult beforeGeneratorSql(Primary primayInfo, UpdateParam param, UpdateResult updateResult) {
		if (primayInfo == null) {
			updateResult.setCode(UpdateResult.WARN);
			String msg = updateResult.getMsg();
			if (msg == null)
				msg = "";
			msg = msg + "<br>表：" + param.getTableName() + "没有主键，限制其修改一条记录";
			updateResult.setMsg(msg);
		}
		return updateResult;
	}

	private SqlParam getSqlParam(Connection connect, UpdateParam param, Primary primayInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		String tableInfo = JdbcUtil.getTableNameInfo(connect, param.getCatalog(), param.getSchema(),
				param.getTableName());
		sb.append(tableInfo);
		List<ChangeValue> values = param.getValues();
		List<Object> zwf = new ArrayList<>();
		// 拼装set值
		String set = getSqlSet(zwf, values);
		sb.append(set);
		// 拼装where条件
		String where = getSqlWhere(zwf, values, primayInfo);
		sb.append(where);
		SqlParam result = new SqlParam();
		result.setCatalog(param.getCatalog());
		result.setSchema(param.getSchema());
		result.setSql(sb.toString());
		result.setParam(zwf.toArray());
		return result;
	}

	@SuppressWarnings("rawtypes")
	private String getSqlSet(List<Object> zwf, List<ChangeValue> values) {
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		TypeHandler typeHandler = builder.getTypeHandler();
		StringBuilder sb = new StringBuilder();
		sb.append(" set ");
		boolean isChanged = false;
		for (ChangeValue ch : values) {
			if (!Objects.equals(ch.getNewValue(), ch.getOldValue())) {
				sb.append(ch.getColumnName());
				sb.append("= ?,");
				isChanged = true;
				zwf.add(typeHandler.getParam(ch.getNewValue(), JdbcType.forCode(ch.getDataType())));
			}
		}
		if (!isChanged) {
			throw new UpdateDataException("没有修改值");
		}
		// 去掉最后一个逗号
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 
	* @author yzh
	* @date 2019年6月30日
	* @Description:  获取where条件
	*  @param zwf 对应预定义参数的值
	*  @param values 修改的值
	*  @param primayInfo
	* @return String   返回where的sql语句
	* @throws
	 */
	@SuppressWarnings("rawtypes")
	private String getSqlWhere(List<Object> zwf, List<ChangeValue> values, Primary primayInfo) {
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

	private UpdateParam getByTableName(List<UpdateParam> updates, String tableName) {
		for (UpdateParam param : updates) {
			if (StringUtils.equals(param.getTableName(), tableName)) {
				return param;
			}
		}
		return null;
	}

}
