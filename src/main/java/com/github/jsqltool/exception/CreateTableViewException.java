package com.github.jsqltool.exception;

/**
 * 获取创建语句失败时抛出的异常
 * @author yzh
 * @date 2019年7月12日
 */
public class CreateTableViewException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CreateTableViewException() {
		super();
	}

	public CreateTableViewException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CreateTableViewException(String message, Throwable cause) {
		super(message, cause);
	}

	public CreateTableViewException(String message) {
		super(message);
	}

	public CreateTableViewException(Throwable cause) {
		super(cause);
	}

}
