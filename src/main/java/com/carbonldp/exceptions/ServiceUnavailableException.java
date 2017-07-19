package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ServiceUnavailableException extends HTTPResponseException {
	public ServiceUnavailableException() {
		this( null );
	}

	public ServiceUnavailableException( Model errorObject ) {
		super( HTTPClient.StatusCode.SERVICE_UNAVAILABLE.getCode(), errorObject );
	}
}