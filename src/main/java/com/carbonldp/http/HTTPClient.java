package com.carbonldp.http;

import com.carbonldp.HTTPResult;

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
import java.util.function.Function;

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

		public <T> CompletableFuture<HTTPResult<T>> send( Function<OpenHTTPResponse, T> responseHandler ) {
			return send( responseHandler, this.retries, 0 );
		}

		// TODO: Handle recoverable HTTPExceptions
		@SuppressWarnings( "unchecked" )
		private <T> CompletableFuture<HTTPResult<T>> send( Function<OpenHTTPResponse, T> responseHandler, int retries, int currentRetries ) {
			HttpURLConnection connection = createHttpURLConnection();

			return CompletableFuture
				.supplyAsync( () -> {
					try {
						connection.connect();
					} catch ( IOException e ) {
						throw new UncheckedIOException( e );
					}

					try {
						OpenHTTPResponse response = new OpenHTTPResponse( connection );

						// TODO: Retry on specific status codes

						T result = responseHandler.apply( response );

						// Casting the response to an object so exceptionally can return CompletableFutures or Throwables
						return (Object) new HTTPResult<>( response, result );
					} finally {
						connection.disconnect();
					}
				} )
				.exceptionally( throwable -> {
					if ( ( throwable instanceof UncheckedIOException ) ) {
						if ( currentRetries == retries ) return throwable;
						return this.send( responseHandler, retries, currentRetries + 1 );
					}

					return throwable;
				} )
				.thenCompose( result -> {
					if ( result instanceof HTTPResult ) {
						return CompletableFuture.completedFuture( (HTTPResult) result );
					} else if ( result instanceof CompletableFuture ) {
						return (CompletableFuture<HTTPResult<T>>) result;
					} else if ( result instanceof RuntimeException ) {
						CompletableFuture<HTTPResult<T>> future = new CompletableFuture<>();
						future.completeExceptionally( (Throwable) result );
						return future;
					} else {
						CompletableFuture<HTTPResult<T>> future = new CompletableFuture<>();
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

		@Override
		public HTTPRequestWithBody header( String header, String value ) {
			super.header( header, value );
			return this;
		}

		@Override
		public HTTPRequestWithBody header( String header, String value, boolean resetValues ) {
			super.header( header, value, resetValues );
			return this;
		}

		@Override
		public HTTPRequestWithBody connectTimeout( int timeout ) {
			super.connectTimeout( timeout );
			return this;
		}

		@Override
		public HTTPRequestWithBody readTimeout( int timeout ) {
			super.readTimeout( timeout );
			return this;
		}

		@Override
		public HTTPRequestWithBody retries( int retries ) {
			super.retries( retries );
			return this;
		}
	}

	public static class ClosedHTTPResponse {
		private final int statusCode;
		private final Map<String, List<String>> headers;

		private ClosedHTTPResponse( HttpURLConnection connection ) {
			try {
				this.statusCode = connection.getResponseCode();
			} catch ( IOException e ) {
				throw new UncheckedIOException( e );
			}
			this.headers = cleanHeaderKeys( connection.getHeaderFields() );
		}

		public int getStatusCode() {
			return this.statusCode;
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

	public static class OpenHTTPResponse extends ClosedHTTPResponse {
		private final HttpURLConnection connection;

		private OpenHTTPResponse( HttpURLConnection connection ) {
			super( connection );
			this.connection = connection;
		}

		public InputStream getBody() {
			try {
				return this.connection.getInputStream();
			} catch ( IOException e ) {
				throw new UncheckedIOException( e );
			}
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
		// 2xx
		OK( 200 ),
		CREATED( 201 ),
		NO_CONTENT( 204 ),

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
