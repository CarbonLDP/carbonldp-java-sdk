package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class HTTPVersionNotSupportedException extends HTTPResponseException {
	public HTTPVersionNotSupportedException() {
		this( null );
	}

	public HTTPVersionNotSupportedException( Model errorObject ) {
		super( HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED.code(), errorObject );
	}
}