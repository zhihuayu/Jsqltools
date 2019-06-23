package com.github.jsqltool.exception;

public class CannotParseUrlException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CannotParseUrlException() {
		super();
	}

	public CannotParseUrlException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CannotParseUrlException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotParseUrlException(String message) {
		super(message);
	}

	public CannotParseUrlException(Throwable cause) {
		super(cause);
	}

}
