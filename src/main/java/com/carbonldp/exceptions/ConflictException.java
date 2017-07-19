package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ConflictException extends HTTPResponseException {
	public ConflictException() {
		this( null );
	}

	public ConflictException( Model errorObject ) {
		super( HTTPClient.StatusCode.CONFLICT.getCode(), errorObject );
	}
}