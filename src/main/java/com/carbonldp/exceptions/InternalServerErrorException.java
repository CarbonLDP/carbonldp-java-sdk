package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class InternalServerErrorException extends HTTPResponseException {
	public InternalServerErrorException() {
		this( null );
	}

	public InternalServerErrorException( Model errorObject ) {
		super( HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), errorObject );
	}
}