package com.github.jsqltool.enums;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.exception.CannotParseUrlException;

/**
 * 数据库类型的枚举类
 * 
 * @author yzh
 * @date 2019年6月15日
 */
public enum DBType {
	MYSQL_TYPE, ORACLE_TYPE, SQLSERVER_TYPE, ODBC_TYPE, OTHER_TYPE;

	public static DBType getDBTypeByUrl(String url) {
		if (StringUtils.isBlank(url)) {
			throw new CannotParseUrlException("url 不能为空");
		}
		if (url.startsWith("jdbc:oracle")) {
			return ORACLE_TYPE;
		} else if (url.startsWith("jdbc:mysql")) {
			return MYSQL_TYPE;
		} else if (url.startsWith("jdbc:sqlserver")) {
			return SQLSERVER_TYPE;
		}
		return OTHER_TYPE;
	}

	public static DBType getDBTypeByDriverClassName(String className) {
		if (StringUtils.isBlank(className)) {
			throw new CannotParseUrlException("className 不能为空");
		}
		if (StringUtils.containsIgnoreCase(className, "oracle")) {
			return ORACLE_TYPE;
		} else if (StringUtils.containsIgnoreCase(className, "mysql")) {
			return MYSQL_TYPE;
		} else if (StringUtils.containsIgnoreCase(className, "sqlserver")) {
			return SQLSERVER_TYPE;
		}
		return OTHER_TYPE;
	}

}
