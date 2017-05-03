package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestTimeoutException extends HTTPResponseException {
	public RequestTimeoutException() {
		this( null );
	}

	public RequestTimeoutException( Model errorObject ) {
		super( HttpResponseStatus.REQUEST_TIMEOUT.code(), errorObject );
	}
}