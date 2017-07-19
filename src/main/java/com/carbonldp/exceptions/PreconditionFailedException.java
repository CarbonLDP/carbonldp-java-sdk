package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class PreconditionFailedException extends HTTPResponseException {

	public PreconditionFailedException() {
		this( null );
	}

	public PreconditionFailedException( Model errorObject ) {
		super( HTTPClient.StatusCode.PRECONDITION_FAILED.getCode(), errorObject );
	}
}
