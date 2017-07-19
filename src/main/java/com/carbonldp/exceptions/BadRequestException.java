package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class BadRequestException extends HTTPResponseException {
	public BadRequestException() {
		this( null );
	}

	public BadRequestException( Model errorObject ) {
		super( HTTPClient.StatusCode.BAD_REQUEST.getCode(), errorObject );
	}
}
