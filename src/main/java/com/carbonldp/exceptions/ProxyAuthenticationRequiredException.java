package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ProxyAuthenticationRequiredException extends HTTPResponseException {
	public ProxyAuthenticationRequiredException() {
		this( null );
	}

	public ProxyAuthenticationRequiredException( Model errorObject ) {
		super( HTTPClient.StatusCode.PROXY_AUTHENTICATION_REQUIRED.getCode(), errorObject );
	}
}
