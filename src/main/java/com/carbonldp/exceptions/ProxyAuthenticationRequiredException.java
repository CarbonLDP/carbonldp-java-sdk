package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ProxyAuthenticationRequiredException extends HTTPResponseException {
	public ProxyAuthenticationRequiredException() {
		this( null );
	}

	public ProxyAuthenticationRequiredException( Model errorObject ) {
		super( HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED.code(), errorObject );
	}
}
