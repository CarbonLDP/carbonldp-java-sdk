package com.carbonldp.exceptions;

import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public abstract class HTTPResponseException extends NoStackTraceRuntimeException {
	protected final int statusCode;
	protected final Model errorObject;

	public HTTPResponseException( int statusCode ) {
		this( statusCode, null );
	}

	public HTTPResponseException( int statusCode, Model errorObject ) {
		this.statusCode = statusCode;
		this.errorObject = errorObject;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public Model getErrorObject() {
		return this.errorObject;
	}
}
