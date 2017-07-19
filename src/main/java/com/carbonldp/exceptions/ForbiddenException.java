package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ForbiddenException extends HTTPResponseException {
	public ForbiddenException() {
		this( null );
	}

	public ForbiddenException( Model errorObject ) {
		super( HTTPClient.StatusCode.FORBIDDEN.getCode(), errorObject );
	}
}
