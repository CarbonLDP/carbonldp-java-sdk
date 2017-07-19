package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestEntityTooLargeException extends HTTPResponseException {
	public RequestEntityTooLargeException() {
		this( null );
	}

	public RequestEntityTooLargeException( Model errorObject ) {
		super( HTTPClient.StatusCode.REQUEST_ENTITY_TOO_LARGE.getCode(), errorObject );
	}
}
