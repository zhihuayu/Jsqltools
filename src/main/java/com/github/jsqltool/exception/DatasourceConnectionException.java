package com.github.jsqltool.exception;

/**
 * 数据库连接异常
 * @author yzh
 *
 * @date 2020年7月10日
 */
public class DatasourceConnectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DatasourceConnectionException() {
		super();
	}

	public DatasourceConnectionException(String message) {
		super(String.format("数据库连接异常：[%s]", message));
	}

	public DatasourceConnectionException(String message, Throwable cause) {
		super(String.format("数据库连接异常：[%s]", message), cause);
	}



	public DatasourceConnectionException(Throwable cause) {
		super(cause);
	}

}
