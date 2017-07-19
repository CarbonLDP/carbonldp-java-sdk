package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class GoneException extends HTTPResponseException {
	public GoneException() {
		this( null );
	}

	public GoneException( Model errorObject ) {
		super( HTTPClient.StatusCode.GONE.getCode(), errorObject );
	}
}