package com.carbonldp.auth;

import com.carbonldp.Context;
import com.carbonldp.http.HTTPClient;

import java.util.Base64;

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

	public void addAuthenticationHeaders( HTTPClient.HTTPRequest request ) {
		request.header( "Authorization", "Basic " + new String( Base64.getEncoder().encode( ( this.username + ":" + this.password ).getBytes() ) ) );
	}
}
