package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class NotImplementedException extends HTTPResponseException {
	public NotImplementedException() {
		this( null );
	}

	public NotImplementedException( Model errorObject ) {
		super( HTTPClient.StatusCode.NOT_IMPLEMENTED.getCode(), errorObject );
	}
}