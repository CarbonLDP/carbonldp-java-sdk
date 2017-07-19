package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class GatewayTimeoutException extends HTTPResponseException {
	public GatewayTimeoutException() {
		this( null );
	}

	public GatewayTimeoutException( Model errorObject ) {
		super( HTTPClient.StatusCode.GATEWAY_TIMEOUT.getCode(), errorObject );
	}
}