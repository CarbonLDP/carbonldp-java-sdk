package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class MethodNotAllowedException extends HTTPResponseException {
	public MethodNotAllowedException() {
		this( null );
	}

	public MethodNotAllowedException( Model errorObject ) {
		super( HTTPClient.StatusCode.METHOD_NOT_ALLOWED.getCode(), errorObject );
	}
}