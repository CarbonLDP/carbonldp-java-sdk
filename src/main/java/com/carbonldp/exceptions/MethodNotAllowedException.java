package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class MethodNotAllowedException extends HTTPResponseException {
	public MethodNotAllowedException() {
		this( null );
	}

	public MethodNotAllowedException( Model errorObject ) {
		super( HttpResponseStatus.METHOD_NOT_ALLOWED.code(), errorObject );
	}
}