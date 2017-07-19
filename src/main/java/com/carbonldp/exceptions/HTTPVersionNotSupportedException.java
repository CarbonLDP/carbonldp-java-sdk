package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class HTTPVersionNotSupportedException extends HTTPResponseException {
	public HTTPVersionNotSupportedException() {
		this( null );
	}

	public HTTPVersionNotSupportedException( Model errorObject ) {
		super( HTTPClient.StatusCode.HTTP_VERSION_NOT_SUPPORTED.getCode(), errorObject );
	}
}