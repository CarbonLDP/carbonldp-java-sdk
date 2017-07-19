package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class LengthRequiredException extends HTTPResponseException {
	public LengthRequiredException() {
		this( null );
	}

	public LengthRequiredException( Model errorObject ) {
		super( HTTPClient.StatusCode.LENGTH_REQUIRED.getCode(), errorObject );
	}
}