package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class BadGatewayException extends HTTPResponseException {
	public BadGatewayException() {
		this( null );
	}

	public BadGatewayException( Model errorObject ) {
		super( HttpResponseStatus.BAD_GATEWAY.code(), errorObject );
	}
}