package com.github.jsqltool.exception;

public class DatasourceNullException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DatasourceNullException() {
		super();
	}

	public DatasourceNullException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DatasourceNullException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatasourceNullException(String message) {
		super(message);
	}

	public DatasourceNullException(Throwable cause) {
		super(cause);
	}

}
