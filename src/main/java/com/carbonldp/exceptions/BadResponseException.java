package com.carbonldp.exceptions;

/**
 * @author MiguelAraCo
 */
public class BadResponseException extends RuntimeException {
	public BadResponseException( String message ) {
		super( message );
	}

	public BadResponseException( String message, Throwable cause ) {
		super( message, cause );
	}

	public BadResponseException( Throwable cause ) {
		super( cause );
	}
}
