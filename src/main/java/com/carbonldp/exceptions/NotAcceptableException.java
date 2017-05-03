package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotAcceptableException extends HTTPResponseException {
	public NotAcceptableException() {
		this( null );
	}

	public NotAcceptableException( Model errorObject ) {
		super( HttpResponseStatus.NOT_ACCEPTABLE.code(), errorObject );
	}
}