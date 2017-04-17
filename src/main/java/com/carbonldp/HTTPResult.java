package com.carbonldp;

import org.asynchttpclient.Response;

/**
 * @author MiguelAraCo
 */
public class HTTPResult<T> {
	public final Response response;
	public final T body;

	public HTTPResult( Response response, T body ) {
		this.response = response;
		this.body = body;
	}
}
