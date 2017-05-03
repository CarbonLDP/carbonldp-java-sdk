package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ExpectationFailedException extends HTTPResponseException {
	public ExpectationFailedException() {
		this( null );
	}

	public ExpectationFailedException( Model errorObject ) {
		super( HttpResponseStatus.EXPECTATION_FAILED.code(), errorObject );
	}
}