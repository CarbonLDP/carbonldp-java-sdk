package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestedRangeNotSatisfiableException extends HTTPResponseException {
	public RequestedRangeNotSatisfiableException() {
		this( null );
	}

	public RequestedRangeNotSatisfiableException( Model errorObject ) {
		super( HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE.code(), errorObject );
	}
}