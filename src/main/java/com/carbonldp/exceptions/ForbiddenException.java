package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ForbiddenException extends HTTPResponseException {
	public ForbiddenException() {
		this( null );
	}

	public ForbiddenException( Model errorObject ) {
		super( HttpResponseStatus.FORBIDDEN.code(), errorObject );
	}
}
