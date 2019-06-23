package com.github.jsqltool.exception;

/**
 * 当没有找到链接的配置文件时会报错
 * 
 * @author yzh
 *
 * @date 2019年6月15日
 */
public class SqlParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SqlParseException() {
		super();
	}

	public SqlParseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SqlParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlParseException(String message) {
		super(message);
	}

	public SqlParseException(Throwable cause) {
		super(cause);
	}

}
