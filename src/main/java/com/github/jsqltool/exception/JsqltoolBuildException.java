package com.github.jsqltool.exception;

public class JsqltoolBuildException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JsqltoolBuildException() {
		super();
	}

	public JsqltoolBuildException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsqltoolBuildException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsqltoolBuildException(String message) {
		super(message);
	}

	public JsqltoolBuildException(Throwable cause) {
		super(cause);
	}

}
