package com.carbonldp.exceptions;

/**
 * @author MiguelAraCo
 */
public abstract class HTTPResponseException extends NoStackTraceRuntimeException {
	protected final int statusCode;

	public HTTPResponseException( int statusCode ) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return this.statusCode;
	}
}
