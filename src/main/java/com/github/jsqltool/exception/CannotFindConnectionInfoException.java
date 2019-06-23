package com.github.jsqltool.exception;

/**
 * 当没有找到链接的配置文件时会报错
 * 
 * @author yzh
 *
 * @date 2019年6月15日
 */
public class CannotFindConnectionInfoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CannotFindConnectionInfoException() {
		super();
	}

	public CannotFindConnectionInfoException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CannotFindConnectionInfoException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotFindConnectionInfoException(String message) {
		super(message);
	}

	public CannotFindConnectionInfoException(Throwable cause) {
		super(cause);
	}

}
