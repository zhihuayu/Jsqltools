package com.github.jsqltool.exception;

public class JsqltoolParamException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JsqltoolParamException() {
		super();
	}

	public JsqltoolParamException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsqltoolParamException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsqltoolParamException(String message) {
		super(message);
	}

	public JsqltoolParamException(Throwable cause) {
		super(cause);
	}

}
