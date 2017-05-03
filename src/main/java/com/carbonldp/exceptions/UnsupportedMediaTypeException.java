package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.eclipse.rdf4j.model.Model;

/**
 * @author MiguelAraCo
 */
public class UnsupportedMediaTypeException extends HTTPResponseException {
	public UnsupportedMediaTypeException() {
		this( null );
	}

	public UnsupportedMediaTypeException( Model errorObject ) {
		super( HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.code(), errorObject );
	}
}