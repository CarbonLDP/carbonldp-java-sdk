package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class LengthRequiredException extends HTTPResponseException {
	public LengthRequiredException() {
		this( null );
	}

	public LengthRequiredException( Model errorObject ) {
		super( HttpResponseStatus.LENGTH_REQUIRED.code(), errorObject );
	}
}