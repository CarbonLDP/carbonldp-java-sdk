package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class GatewayTimeoutException extends HTTPResponseException {
	public GatewayTimeoutException() {
		this( null );
	}

	public GatewayTimeoutException( Model errorObject ) {
		super( HttpResponseStatus.GATEWAY_TIMEOUT.code(), errorObject );
	}
}