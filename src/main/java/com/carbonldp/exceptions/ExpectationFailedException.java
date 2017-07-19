package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class ExpectationFailedException extends HTTPResponseException {
	public ExpectationFailedException() {
		this( null );
	}

	public ExpectationFailedException( Model errorObject ) {
		super( HTTPClient.StatusCode.EXPECTATION_FAILED.getCode(), errorObject );
	}
}