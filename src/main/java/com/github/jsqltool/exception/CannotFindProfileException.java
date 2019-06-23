package com.github.jsqltool.exception;

/**
 * 当没有找到链接的配置文件时会报错
 * 
 * @author yzh
 *
 * @date 2019年6月15日
 */
public class CannotFindProfileException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CannotFindProfileException() {
		super();
	}

	public CannotFindProfileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CannotFindProfileException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotFindProfileException(String message) {
		super(message);
	}

	public CannotFindProfileException(Throwable cause) {
		super(cause);
	}

}
