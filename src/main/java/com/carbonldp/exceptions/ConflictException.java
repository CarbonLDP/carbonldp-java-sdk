package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ConflictException extends HTTPResponseException {
	public ConflictException() {
		this( null );
	}

	public ConflictException( Model errorObject ) {
		super( HttpResponseStatus.CONFLICT.code(), errorObject );
	}
}