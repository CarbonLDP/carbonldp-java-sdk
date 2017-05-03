package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotFoundException extends HTTPResponseException {
	public NotFoundException() {
		this( null );
	}

	public NotFoundException( Model errorObject ) {
		super( HttpResponseStatus.NOT_FOUND.code(), errorObject );
	}
}
