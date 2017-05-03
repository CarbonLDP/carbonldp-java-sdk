package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class GoneException extends HTTPResponseException {
	public GoneException() {
		this( null );
	}

	public GoneException( Model errorObject ) {
		super( HttpResponseStatus.GONE.code(), errorObject );
	}
}