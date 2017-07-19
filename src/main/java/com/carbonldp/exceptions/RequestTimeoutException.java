package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestTimeoutException extends HTTPResponseException {
	public RequestTimeoutException() {
		this( null );
	}

	public RequestTimeoutException( Model errorObject ) {
		super( HTTPClient.StatusCode.REQUEST_TIMEOUT.getCode(), errorObject );
	}
}