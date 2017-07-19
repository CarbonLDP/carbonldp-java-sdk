package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class RequestURITooLongException extends HTTPResponseException {
	public RequestURITooLongException() {
		this( null );
	}

	public RequestURITooLongException( Model errorObject ) {
		super( HTTPClient.StatusCode.REQUEST_URI_TOO_LONG.getCode(), errorObject );
	}
}
