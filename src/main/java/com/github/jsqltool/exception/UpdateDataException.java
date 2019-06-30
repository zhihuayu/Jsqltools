package com.github.jsqltool.exception;

/**
 * 更新数据表数据时发生的异常
 * 
 * @author yzh
 * @date 2019年6月28日
 */
public class UpdateDataException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UpdateDataException() {
		super();
	}

	public UpdateDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UpdateDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpdateDataException(String message) {
		super(message);
	}

	public UpdateDataException(Throwable cause) {
		super(cause);
	}

}
