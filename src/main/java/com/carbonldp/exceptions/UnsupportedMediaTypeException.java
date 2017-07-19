package com.carbonldp.exceptions;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class UnsupportedMediaTypeException extends HTTPResponseException {
	public UnsupportedMediaTypeException() {
		this( null );
	}

	public UnsupportedMediaTypeException( Model errorObject ) {
		super( HTTPClient.StatusCode.UNSUPPORTED_MEDIA_TYPE.getCode(), errorObject );
	}
}