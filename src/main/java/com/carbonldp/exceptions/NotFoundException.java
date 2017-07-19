package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotFoundException extends HTTPResponseException {
	public NotFoundException() {
		this( null );
	}

	public NotFoundException( Model errorObject ) {
		super( HTTPClient.StatusCode.NOT_FOUND.getCode(), errorObject );
	}
}
