package com.github.jsqltool.param;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.exception.JsqltoolParamException;

/**
 *   存储过程的参数，值得注意的是：
 *     1）如果带有返回值，那么参数必须使用索引类的形式；
 *     2）如果没有带有返回，那么命名参数和索引列不能混用，只能选用一种方式；
 * @author yzh
 * @date 2019年8月6日
 */
public class ProcedureParam {

	private String catalog;
	private String schema;
	private String procedureName;
	// 如果没有返回值则为NULL
	private JdbcType returnType;
	private List<P_Param> params;

	public static void checkParam(P_Param p) {
		if (p == null) {
			throw new JsqltoolParamException("参数不能为空！");
		}
		if (StringUtils.isBlank(p.getParamName())
				&& (p.getParamIndex() == null || p.getParamIndex().compareTo(0) <= 0)) {
			throw new JsqltoolParamException("参数索引或者参数名称不能为空！");
		}
		if (p.getDataType() == null) {
			throw new JsqltoolParamException("参数的数据类型不能为空！");
		}
		if (StringUtils.isBlank(p.getType())) {
			throw new JsqltoolParamException("参数的类型不能为空！");
		}
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public JdbcType getReturnType() {
		return returnType;
	}

	public void setReturnType(JdbcType returnType) {
		this.returnType = returnType;
	}

	public List<P_Param> getParams() {
		return params;
	}

	public void setParams(List<P_Param> params) {
		this.params = params;
	}

	public static class P_Param {

		private String paramName;
		private Integer paramIndex;
		private JdbcType dataType;
		private String type; // 可选值有IN、OUT、IN OUT
		private Object value;

		public String getParamName() {
			return paramName;
		}

		public void setParamName(String paramName) {
			this.paramName = paramName;
		}

		public Integer getParamIndex() {
			return paramIndex;
		}

		public void setParamIndex(Integer paramIndex) {
			this.paramIndex = paramIndex;
		}

		public JdbcType getDataType() {
			return dataType;
		}

		public void setDataType(JdbcType dataType) {
			this.dataType = dataType;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

	}

}
