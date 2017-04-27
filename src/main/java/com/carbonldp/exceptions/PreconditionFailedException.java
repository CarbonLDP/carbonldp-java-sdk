package com.carbonldp.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author MiguelAraCo
 */
public class PreconditionFailedException extends HTTPResponseException {

	public PreconditionFailedException() {
		super( HttpResponseStatus.PRECONDITION_FAILED.code() );
	}
}
