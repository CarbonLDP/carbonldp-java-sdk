package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestURITooLongException extends HTTPResponseException {
	public RequestURITooLongException() {
		this( null );
	}

	public RequestURITooLongException( Model errorObject ) {
		super( HttpResponseStatus.REQUEST_URI_TOO_LONG.code(), errorObject );
	}
}
