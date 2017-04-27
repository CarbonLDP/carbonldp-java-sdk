package com.carbonldp.utils;

import com.carbonldp.descriptions.APIPreferences;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Map;
import java.util.Optional;

/**
 * @author MiguelAraCo
 */
public final class AsyncHTTPUtils {
	public static Optional<String> getHeader( String headerName, Response response ) {
		if ( headerName == null ) throw new IllegalArgumentException( "'headerName' can't be null" );
		if ( response == null ) throw new IllegalArgumentException( "'response' can't be null" );

		return getHeader( headerName, response.getHeaders() );
	}

	public static Optional<String> getHeader( String headerName, Request request ) {
		if ( headerName == null ) throw new IllegalArgumentException( "'headerName' can't be null" );
		if ( request == null ) throw new IllegalArgumentException( "'request' can't be null" );

		return getHeader( headerName, request.getHeaders() );
	}

	private static Optional<String> getHeader( String headerName, HttpHeaders headers ) {
		for ( Map.Entry<String, String> header : headers ) {
			String headerKey = header.getKey();
			if ( headerName.toLowerCase().equals( headerKey.toLowerCase() ) ) return Optional.ofNullable( header.getValue() );
		}
		return Optional.empty();
	}

	public static void setAcceptHeader( BoundRequestBuilder request ) {
		request.setHeader( "Accept", "application/ld+json" );
	}

	public static void setContentTypeHeader( BoundRequestBuilder request ) {
		request.setHeader( "Content-Type", "application/ld+json" );
	}

	public static void setInteractionModel( BoundRequestBuilder request, APIPreferences.InteractionModel interactionModel ) {
		request.setHeader( "Prefer", interactionModel.getIRI().toString() + "; rel=interaction-model" );
	}

	public static void setIfMatch( BoundRequestBuilder request, String eTag ) {
		request.setHeader( "If-Match", eTag );
	}

	public static void setSlug( BoundRequestBuilder request, String slug ) {
		request.setHeader( "Slug", slug );
	}

	public static Optional<String> getETag( Response response ) {
		return getHeader( "ETag", response );
	}
}
