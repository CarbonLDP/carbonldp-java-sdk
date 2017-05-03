package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ServiceUnavailableException extends HTTPResponseException {
	public ServiceUnavailableException() {
		this( null );
	}

	public ServiceUnavailableException( Model errorObject ) {
		super( HttpResponseStatus.SERVICE_UNAVAILABLE.code(), errorObject );
	}
}