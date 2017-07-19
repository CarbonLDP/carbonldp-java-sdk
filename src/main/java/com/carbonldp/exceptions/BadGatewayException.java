package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class BadGatewayException extends HTTPResponseException {
	public BadGatewayException() {
		this( null );
	}

	public BadGatewayException( Model errorObject ) {
		super( HTTPClient.StatusCode.BAD_GATEWAY.getCode(), errorObject );
	}
}