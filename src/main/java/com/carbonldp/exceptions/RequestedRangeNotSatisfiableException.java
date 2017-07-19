package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestedRangeNotSatisfiableException extends HTTPResponseException {
	public RequestedRangeNotSatisfiableException() {
		this( null );
	}

	public RequestedRangeNotSatisfiableException( Model errorObject ) {
		super( HTTPClient.StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE.getCode(), errorObject );
	}
}