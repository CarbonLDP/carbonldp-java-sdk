package com.carbonldp.auth;

import com.carbonldp.Context;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.util.Base64;

/**
 * @author MiguelAraCo
 */
public class AuthService {
	private static final String AUTH_TOKENS = "auth-tokens/";

	private final Context context;
	// TODO: Use JWT instead of basic authentication
	private String username;
	private String password;

	public AuthService( Context context ) {
		this.context = context;
	}

	public boolean isAuthenticated() {
		return this.username != null && this.password != null;
	}

	public void authenticate( String username, String password ) {
		this.username = username;
		this.password = password;
	}

	public void addAuthenticationHeaders( BoundRequestBuilder request ) {
		if ( ! isAuthenticated() ) return;

		request.setHeader( "Authorization", "Basic " + Base64.encode( ( this.username + ":" + this.password ).getBytes() ) );
	}
}
