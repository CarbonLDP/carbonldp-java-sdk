package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class BadRequestException extends HTTPResponseException {
	public BadRequestException() {
		this( null );
	}

	public BadRequestException( Model errorObject ) {
		super( HttpResponseStatus.BAD_REQUEST.code(), errorObject );
	}
}
