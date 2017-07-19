package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class InternalServerErrorException extends HTTPResponseException {
	public InternalServerErrorException() {
		this( null );
	}

	public InternalServerErrorException( Model errorObject ) {
		super( HTTPClient.StatusCode.INTERNAL_SERVER_ERROR.getCode(), errorObject );
	}
}