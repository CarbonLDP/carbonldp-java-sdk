package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestEntityTooLargeException extends HTTPResponseException {
	public RequestEntityTooLargeException() {
		this( null );
	}

	public RequestEntityTooLargeException( Model errorObject ) {
		super( HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code(), errorObject );
	}
}
