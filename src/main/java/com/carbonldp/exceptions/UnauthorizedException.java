package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class UnauthorizedException extends HTTPResponseException {
	public UnauthorizedException() {
		this( null );
	}

	public UnauthorizedException( Model errorObject ) {
		super( HttpResponseStatus.UNAUTHORIZED.code(), errorObject );
	}
}