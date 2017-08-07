package com.carbonldp.http;

import com.carbonldp.HTTPResult;
import org.apache.commons.codec.Charsets;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
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

	public static HTTPPostRequest post( String url ) {
		return new HTTPPostRequest( "POST", url );
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

		protected String method;
		protected URL url;
		protected Map<String, List<String>> headers = new HashMap<>();
		protected Integer connectTimeout = null;
		protected Integer readTimeout = null;
		protected int retries;

		protected HTTPRequest( String method, String url ) {
			this.method = method;

			try {
				this.url = new URL( url );
			} catch ( MalformedURLException e ) {
				throw new IllegalArgumentException( "The provided URL is not valid", e );
			}

			String protocol = this.url.getProtocol();
			if ( ! "http".equals( protocol ) && ! "https".equals( protocol ) ) throw new IllegalArgumentException( "The protocol '" + protocol + "' of the provided URL is not supported" );
		}

		// Used while cloning requests
		protected HTTPRequest() {}

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

		protected void beforeWritingHeaders( HttpURLConnection connection ) {
			// Nothing to do, but it can be overridden by sub-classes
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

			beforeWritingHeaders( connection );

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
		protected HTTPRequestWithBody( String method, String url ) {
			super( method, url );
		}

		protected HTTPRequestWithBody() {}

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

	public static class HTTPPostRequest extends HTTPRequestWithBody {
		protected HTTPPostRequest( String method, String url ) {
			super( method, url );
		}

		public HTTPMultipartRequest multipart() {
			HTTPMultipartRequest request = new HTTPMultipartRequest();
			request.method = this.method;
			request.url = this.url;
			request.headers = this.headers;
			request.connectTimeout = this.connectTimeout;
			request.readTimeout = this.readTimeout;
			request.retries = this.retries;
			return request;
		}

		@Override
		public HTTPPostRequest body( Consumer<OutputStream> bodyWriter ) {
			super.body( bodyWriter );
			return this;
		}

		@Override
		public HTTPPostRequest header( String header, String value ) {
			super.header( header, value );
			return this;
		}

		@Override
		public HTTPPostRequest header( String header, String value, boolean resetValues ) {
			super.header( header, value, resetValues );
			return this;
		}

		@Override
		public HTTPPostRequest connectTimeout( int timeout ) {
			super.connectTimeout( timeout );
			return this;
		}

		@Override
		public HTTPPostRequest readTimeout( int timeout ) {
			super.readTimeout( timeout );
			return this;
		}

		@Override
		public HTTPPostRequest retries( int retries ) {
			super.retries( retries );
			return this;
		}
	}

	public static class FileField {
		protected String fieldName;
		protected String fileName;
		protected String fileContentType;
		protected Consumer<OutputStream> fileWriter;
	}

	public static class HTTPMultipartRequest extends HTTPRequest {
		private static final String LINE_FEED = "\r\n";

		protected Charset charset = Charsets.UTF_8;
		protected String boundary;
		protected Map<String, String> fields = new HashMap<>();
		protected List<FileField> fileFields = new ArrayList<>();
		protected int chunkSize = 1024 * 1024 * 10;

		protected HTTPMultipartRequest( String method, String url ) {
			super( method, url );
			this.bodyWriter = this::writeMultipartBody;
		}

		protected HTTPMultipartRequest() {
			this.bodyWriter = this::writeMultipartBody;
		}

		public HTTPMultipartRequest field( String name, String value ) {
			this.fields.put( name, value );
			return this;
		}

		public HTTPMultipartRequest file( String field, String name, Consumer<OutputStream> writer ) {
			String contentType = URLConnection.guessContentTypeFromName( name );
			return file( field, name, writer, contentType );
		}

		public HTTPMultipartRequest file( String field, String name, Consumer<OutputStream> writer, String contentType ) {
			FileField fileField = new FileField();
			fileField.fieldName = field;
			fileField.fileName = name;
			fileField.fileContentType = contentType;
			fileField.fileWriter = writer;

			this.fileFields.add( fileField );

			return this;
		}

		public HTTPMultipartRequest chunkSize( int chunkSize ) {
			this.chunkSize = chunkSize;
			return this;
		}

		@Override
		protected void beforeWritingHeaders( HttpURLConnection connection ) {
			super.beforeWritingHeaders( connection );

			// HttpURLConnection uses a byte array to store the content of the request's body
			// before sending it. If the file is big enough, it can cause an OutOfMemoryException.
			// Calling this method sets the connection to streaming mode instead, meaning the connection
			// will send data after the threshold is reached (instead of pulling everything into
			// memory.
			// TODO: This is not supported by all servers, switch it to something more widely supported
			if ( this.fileFields.size() > 0 ) connection.setChunkedStreamingMode( this.chunkSize );

			setBoundary();
		}

		protected void setBoundary() {
			this.boundary = "-===" + System.currentTimeMillis() + "===-";
			this.header( "Content-Type", "multipart/form-data; boundary=" + this.boundary, true );
		}

		private void writeMultipartBody( OutputStream outputStream ) {
			PrintWriter writer = new PrintWriter( new OutputStreamWriter( outputStream, this.charset ), true );

			writeFields( writer );
			writeFiles( writer, outputStream );
			finishBody( writer );
		}

		private void writeFields( PrintWriter writer ) {
			for ( Map.Entry<String, String> field : this.fields.entrySet() ) {
				writeField( field.getKey(), field.getValue(), writer );
			}
		}

		private void writeField( String name, String value, PrintWriter writer ) {
			writer
				.append( "--" ).append( boundary ).append( LINE_FEED )
				.append( "Content-Disposition: form-data; name=\"" ).append( name ).append( "\"" ).append( LINE_FEED )
				.append( "Content-Type: text/plain; charset=" ).append( this.charset.toString() ).append( LINE_FEED )
				.append( LINE_FEED )
				.append( value ).append( LINE_FEED );

			writer.flush();
		}

		private void writeFiles( PrintWriter writer, OutputStream bodyOutputStream ) {
			for ( FileField fileField : this.fileFields ) {
				writeFile( fileField, writer, bodyOutputStream );
			}
		}

		private void writeFile( FileField fileField, PrintWriter writer, OutputStream bodyOutputStream ) {
			writer
				.append( "--" ).append( boundary ).append( LINE_FEED )
				.append( "Content-Disposition: form-data; name=\"" ).append( fileField.fieldName ).append( "\"; filename=\"" ).append( fileField.fileName ).append( "\"" ).append( LINE_FEED )
				.append( "Content-Type: " ).append( fileField.fileContentType ).append( LINE_FEED )
				.append( "Content-Transfer-Encoding: binary" ).append( LINE_FEED )
				.append( LINE_FEED );

			writer.flush();

			fileField.fileWriter.accept( bodyOutputStream );

			try {
				bodyOutputStream.flush();
			} catch ( IOException e ) {
				throw new UncheckedIOException( "Couldn't flush request's body's output stream after writing file", e );
			}

			writer.append( LINE_FEED );
			writer.flush();
		}

		private void finishBody( PrintWriter writer ) {
			writer
				.append( LINE_FEED ).flush();
			writer
				.append( "--" ).append( boundary ).append( "--" ).append( LINE_FEED );

			writer.close();
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
