package com.carbonldp;

import com.carbonldp.http.HTTPClient;

/**
 * @author MiguelAraCo
 */
public class HTTPResult<T> {
	public final HTTPClient.ClosedHTTPResponse response;
	public final T body;

	public HTTPResult( HTTPClient.ClosedHTTPResponse response, T body ) {
		this.response = response;
		this.body = body;
	}
}
