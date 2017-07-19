package com.carbonldp.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author MiguelAraCo
 */
public class HTTPClient {
	public static HTTPRequest get( String url ) {
		return new HTTPRequest( "GET", url );
	}

	public static HTTPRequestWithBody post( String url ) {
		return new HTTPRequestWithBody( "POST", url );
	}

	public static HTTPRequestWithBody put( String url ) {
		return new HTTPRequestWithBody( "PUT", url );
	}

	public static HTTPRequestWithBody patch( String url ) {
		return new HTTPRequestWithBody( "PATCH", url );
	}

	public static HTTPRequestWithBody delete( String url ) {
		return new HTTPRequestWithBody( "DELETE", url );
	}

	// TODO: Make it thread safe
	// TODO: Modify it so modifications made after executing a send don't affect ongoing requests
	public static class HTTPRequest {
		protected Consumer<OutputStream> bodyWriter;

		private final String method;
		private final URL url;
		private final Map<String, List<String>> headers = new HashMap<>();
		private Integer connectTimeout = null;
		private Integer readTimeout = null;
		private int retries;

		private HTTPRequest( String method, String url ) {
			this.method = method;

			try {
				this.url = new URL( url );
			} catch ( MalformedURLException e ) {
				throw new IllegalArgumentException( "The provided URL is not valid", e );
			}

			String protocol = this.url.getProtocol();
			if ( ! "http".equals( protocol ) && ! "https".equals( protocol ) ) throw new IllegalArgumentException( "The protocol '" + protocol + "' of the provided URL is not supported" );
		}

		public HTTPRequest header( String header, String value ) {
			return header( header, value, false );
		}

		public HTTPRequest header( String header, String value, boolean resetValues ) {
			header = cleanHeaderName( header );

			List<String> values = null;
			if ( ! resetValues ) values = this.headers.get( header );
			if ( values == null ) values = new ArrayList<>();
			values.add( value );

			this.headers.put( header, values );

			return this;
		}

		public HTTPRequest connectTimeout( int timeout ) {
			if ( timeout < 0 ) throw new IllegalArgumentException( "The timeout can't be negative" );

			return this;
		}

		public HTTPRequest readTimeout( int timeout ) {
			if ( timeout < 0 ) throw new IllegalArgumentException( "The timeout can't be negative" );
			this.readTimeout = timeout;

			return this;
		}

		public HTTPRequest retries( int retries ) {
			if ( retries < 0 ) throw new IllegalArgumentException( "The number of retries can't be negative" );

			this.retries = retries;

			return this;
		}

		public CompletableFuture<HTTPResponse> send() {
			return send( this.retries, 0 );
		}

		// TODO: Handle recoverable HTTPExceptions
		@SuppressWarnings( "unchecked" )
		private CompletableFuture<HTTPResponse> send( int retries, int currentRetries ) {
			HttpURLConnection connection = createHttpURLConnection();

			return CompletableFuture
				.supplyAsync( () -> {
					int statusCode;
					try {
						connection.connect();
						statusCode = connection.getResponseCode();
					} catch ( IOException e ) {
						throw new UncheckedIOException( e );
					}

					// TODO: Retry on specific status codes

					// Casting the response to an object so exceptionally can return CompletableFutures or Throwables
					return (Object) new HTTPResponse( connection );
				} )
				.exceptionally( throwable -> {
					if ( ( throwable instanceof UncheckedIOException ) ) {
						if ( currentRetries == retries ) return throwable;
						return this.send( retries, currentRetries + 1 );
					}

					return throwable;
				} )
				.thenCompose( result -> {
					if ( result instanceof HTTPResponse ) {
						return CompletableFuture.completedFuture( (HTTPResponse) result );
					} else if ( result instanceof CompletableFuture ) {
						return (CompletableFuture<HTTPResponse>) result;
					} else if ( result instanceof RuntimeException ) {
						CompletableFuture<HTTPResponse> future = new CompletableFuture<>();
						future.completeExceptionally( (Throwable) result );
						return future;
					} else {
						CompletableFuture<HTTPResponse> future = new CompletableFuture<>();
						future.completeExceptionally( new IllegalStateException() );
						return future;
					}
				} );
		}

		private String cleanHeaderName( String headerName ) {
			return headerName.trim().toLowerCase();
		}

		private HttpURLConnection createHttpURLConnection() {
			HttpURLConnection connection;
			try {
				connection = (HttpURLConnection) this.url.openConnection();
			} catch ( IOException e ) {
				throw new UncheckedIOException( e );
			}

			try {
				connection.setRequestMethod( this.method );
			} catch ( ProtocolException e ) {
				throw new IllegalStateException( e );
			}

			setHeaders( connection );

			if ( this.connectTimeout != null ) connection.setConnectTimeout( this.connectTimeout );
			if ( this.readTimeout != null ) connection.setReadTimeout( this.readTimeout );

			if ( this.bodyWriter != null ) {
				connection.setDoOutput( true );

				OutputStream bodyOutputStream;
				try {
					bodyOutputStream = connection.getOutputStream();

					this.bodyWriter.accept( bodyOutputStream );

					bodyOutputStream.close();
				} catch ( IOException e ) {
					throw new UncheckedIOException( e );
				}
			}

			return connection;
		}

		private void setHeaders( HttpURLConnection connection ) {
			for ( Map.Entry<String, List<String>> headers : this.headers.entrySet() ) {
				String header = headers.getKey();
				List<String> values = headers.getValue();

				for ( String value : values ) {
					connection.addRequestProperty( header, value );
				}
			}
		}
	}

	public static class HTTPRequestWithBody extends HTTPRequest {
		private HTTPRequestWithBody( String method, String url ) {
			super( method, url );
		}

		public HTTPRequestWithBody body( Consumer<OutputStream> bodyWriter ) {
			this.bodyWriter = bodyWriter;

			return this;
		}
	}

	public static class HTTPResponse {
		private final HttpURLConnection connection;
		private final Map<String, List<String>> headers;

		private HTTPResponse( HttpURLConnection connection ) {
			this.connection = connection;
			this.headers = cleanHeaderKeys( this.connection.getHeaderFields() );
		}

		public int getResponseCode() {
			try {
				return this.connection.getResponseCode();
			} catch ( IOException e ) {
				throw new UncheckedIOException( e );
			}
		}

		public String getHeader( String header ) {
			List<String> headerValues = this.getHeaderValues( header );
			if ( headerValues == null || headerValues.size() == 0 ) return null;
			return headerValues.get( 0 );
		}

		public List<String> getHeaderValues( String header ) {
			header = header.trim().toLowerCase();

			return this.headers.get( header );
		}

		public InputStream getBody() {
			try {
				return this.connection.getInputStream();
			} catch ( IOException e ) {
				throw new UncheckedIOException( e );
			}
		}

		public void close() {
			this.connection.disconnect();
		}

		private Map<String, List<String>> cleanHeaderKeys( Map<String, List<String>> headers ) {
			Map<String, List<String>> cleanedHeaders = new HashMap<>();
			for ( Map.Entry<String, List<String>> headerEntry : headers.entrySet() ) {
				String key = headerEntry.getKey();
				List<String> value = headerEntry.getValue();

				if ( key == null || value == null || value.size() == 0 ) continue;

				cleanedHeaders.put( key.toLowerCase(), value );
			}
			return cleanedHeaders;
		}
	}

	private static Map<String, List<String>> cloneHeadersMap( Map<String, List<String>> headersMap ) {
		Map<String, List<String>> clone = new HashMap<>();
		for ( Map.Entry<String, List<String>> entry : headersMap.entrySet() ) {
			String key = entry.getKey();
			List<String> values = entry.getValue();

			if ( values == null || values.size() == 0 ) continue;

			List<String> clonedValues = new ArrayList<>();
			clonedValues.addAll( values );

			clone.put( key, clonedValues );
		}
		return clone;
	}

	public enum StatusCode {
		// 4xx
		BAD_REQUEST( 400 ),
		UNAUTHORIZED( 401 ),
		FORBIDDEN( 403 ),
		NOT_FOUND( 404 ),
		METHOD_NOT_ALLOWED( 405 ),
		NOT_ACCEPTABLE( 406 ),
		PROXY_AUTHENTICATION_REQUIRED( 407 ),
		REQUEST_TIMEOUT( 408 ),
		CONFLICT( 409 ),
		GONE( 410 ),
		LENGTH_REQUIRED( 411 ),
		PRECONDITION_FAILED( 412 ),
		REQUEST_ENTITY_TOO_LARGE( 413 ),
		REQUEST_URI_TOO_LONG( 414 ),
		UNSUPPORTED_MEDIA_TYPE( 415 ),
		REQUESTED_RANGE_NOT_SATISFIABLE( 416 ),
		EXPECTATION_FAILED( 417 ),

		// 5xx
		INTERNAL_SERVER_ERROR( 500 ),
		NOT_IMPLEMENTED( 501 ),
		BAD_GATEWAY( 502 ),
		SERVICE_UNAVAILABLE( 503 ),
		GATEWAY_TIMEOUT( 504 ),
		HTTP_VERSION_NOT_SUPPORTED( 505 );

		public static StatusCode valueOf( int code ) {
			for ( StatusCode statusCode : values() ) {
				if ( statusCode.getCode() == code ) return statusCode;
			}
			return null;
		}

		private final int code;

		StatusCode( int code ) { this.code = code;}

		public int getCode() {return this.code;}
	}
}
