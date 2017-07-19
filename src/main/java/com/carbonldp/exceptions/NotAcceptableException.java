package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotAcceptableException extends HTTPResponseException {
	public NotAcceptableException() {
		this( null );
	}

	public NotAcceptableException( Model errorObject ) {
		super( HTTPClient.StatusCode.NOT_ACCEPTABLE.getCode(), errorObject );
	}
}