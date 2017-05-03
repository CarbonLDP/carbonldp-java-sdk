package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class PreconditionFailedException extends HTTPResponseException {

	public PreconditionFailedException() {
		this( null );
	}

	public PreconditionFailedException( Model errorObject ) {
		super( HttpResponseStatus.PRECONDITION_FAILED.code(), errorObject );
	}
}
