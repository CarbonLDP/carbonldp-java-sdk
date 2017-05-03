package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotImplementedException extends HTTPResponseException {
	public NotImplementedException() {
		this( null );
	}

	public NotImplementedException( Model errorObject ) {
		super( HttpResponseStatus.NOT_IMPLEMENTED.code(), errorObject );
	}
}