package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class UnauthorizedException extends HTTPResponseException {
	public UnauthorizedException() {
		this( null );
	}

	public UnauthorizedException( Model errorObject ) {
		super( HTTPClient.StatusCode.UNAUTHORIZED.getCode(), errorObject );
	}
}