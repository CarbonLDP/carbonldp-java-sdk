package com.carbonldp;

import com.carbonldp.descriptions.APIPreferences;
import com.carbonldp.exceptions.*;
import com.carbonldp.http.HTTPClient;
import com.carbonldp.model.PersistedDocument;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.AbstractModel;

import java.util.concurrent.CompletableFuture;

/**
 * @author MiguelAraCo
 */
public class DocumentService {
	private final Context context;

	public DocumentService( Context context ) {
		this.context = context;
	}

	public CompletableFuture<PersistedDocument> get( String documentIRI ) {
		return get( this.context.resolve( documentIRI ) );
	}

	public CompletableFuture<PersistedDocument> get( IRI documentIRI ) {
		HTTPClient.HTTPRequest request = HTTPClient.get( documentIRI.stringValue() );

		request
			.header( "Accept", "application/ld+json" )
			.header( "Prefer", APIPreferences.InteractionModel.RDF_SOURCE.getIRI().toString() + "; rel=interaction-model" );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		return request
			.send()
			.thenApply( response ->
				verifyResponseStatusCode( response,
					200
				)
			)
			.thenApply( new JSONLDParser.NativeParser() )
			.thenApply( result -> {
				String location = result.response.getHeader( "content-location" );
				if ( location == null ) {
					// TODO
				} else {
					// TODO
				}

				PersistedDocument document = new PersistedDocument( result.body, documentIRI );

				String eTag = result.response.getHeader( "ETag" );
				if ( eTag == null ) throw new BadResponseException( "The response didn't contained an ETag" );

				document.setETag( eTag );

				return document;
			} );
	}

	//	public CompletableFuture<Void> addMember( IRI document, IRI member ) {
//		return addMembers( document, Arrays.asList( member ) );
//	}
//
//	public CompletableFuture<Void> addMembers( IRI document, Collection<IRI> members ) {
//		Document addActionDocument = new Document( new EmptyIRI() );
//
//		Fragment addAction = new Fragment( SimpleValueFactory.getInstance().createBNode(), addActionDocument );
//		addAction.addType( AddMemberAction.CLASS.getIRI() );
//		addAction.add( AddMemberAction.Property.targetMember.getIRI(), members );
//
//		BoundRequestBuilder request = this.httpClient.preparePut( document.toString() );
//
//		AsyncHTTPUtils.setContentTypeHeader( request );
//		AsyncHTTPUtils.setAcceptHeader( request );
//		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.CONTAINER );
//
//		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );
//
//		// TODO: Use DocumentParser instead of using BaseModel
//		JSONLDParser.write( request, addActionDocument.getBaseModel() );
//
//		// TODO: Abstract response handling
//		return request
//			.execute().toCompletableFuture()
//			.thenAccept( response -> {
//				verifyResponseStatusCode( response,
//					HttpResponseStatus.OK.code(),
//					HttpResponseStatus.NO_CONTENT.code()
//				);
//			} );
//	}
//
//	public CompletableFuture<IRI> createChild( Document parentDocument, Document childDocument ) {
//		return createChild( parentDocument, childDocument, null );
//	}
//
//	public CompletableFuture<IRI> createChild( Document parentDocument, Document childDocument, String slug ) {
//		return createChild( parentDocument.getIRI(), childDocument, slug );
//	}
//
//	public CompletableFuture<IRI> createChild( String parentDocument, Document childDocument ) {
//		return createChild( parentDocument, childDocument, null );
//	}
//
//	public CompletableFuture<IRI> createChild( String parentDocument, Document childDocument, String slug ) {
//		return createChild( this.context.resolve( parentDocument ), childDocument, slug );
//	}
//
//	public CompletableFuture<IRI> createChild( IRI parentDocument, Document childDocument ) {
//		return createChild( parentDocument, childDocument, null );
//	}
//
//	public CompletableFuture<IRI> createChild( IRI parentDocument, Document childDocument, String slug ) {
//		return createDocument( parentDocument, childDocument, slug, APIPreferences.InteractionModel.CONTAINER );
//	}
//
//	public CompletableFuture<IRI> createAccessPoint( IRI parentDocument, Document childDocument, String slug ) {
//		return createDocument( parentDocument, childDocument, slug, APIPreferences.InteractionModel.RDF_SOURCE );
//	}
//
//	private CompletableFuture<IRI> createDocument( IRI parentDocument, Document childDocument, String slug, APIPreferences.InteractionModel interactionModel ) {
//		BoundRequestBuilder request = this.httpClient.preparePost( parentDocument.toString() );
//
//		AsyncHTTPUtils.setContentTypeHeader( request );
//		AsyncHTTPUtils.setInteractionModel( request, interactionModel );
//
//		if ( slug != null ) AsyncHTTPUtils.setSlug( request, slug );
//
//		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );
//
//		// TODO: Use DocumentParser instead of using BaseModel
//		JSONLDParser.write( request, childDocument.getBaseModel() );
//
//		return request
//			.execute().toCompletableFuture()
//			.thenApply( response -> {
//				verifyResponseStatusCode( response,
//					HttpResponseStatus.CREATED.code(),
//					HttpResponseStatus.NO_CONTENT.code()
//				);
//
//				Optional<String> location = AsyncHTTPUtils.getHeader( "Location", response );
//				if ( ! location.isPresent() ) throw new BadResponseException( "The response didn't include a location header" );
//
//				return this.context.getValueFactory().createIRI( location.get() );
//			} );
//	}
//
	public CompletableFuture<Void> save( PersistedDocument document ) {
		HTTPClient.HTTPRequestWithBody request = HTTPClient.put( document.getIRI().toString() );

		request
			.header( "Accept", "application/ld+json" )
			.header( "Prefer", APIPreferences.InteractionModel.RDF_SOURCE.getIRI().toString() + "; rel=interaction-model" )
			.header( "If-Match", document.getETag() )
		;

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		JSONLDParser.NativeWriter.setContentType( request );

		return request
			.body( new JSONLDParser.NativeWriter( document.getGraph() ) )
			.send()
			.thenAccept( response -> {
				verifyResponseStatusCode( response,
					200,
					204
				);

				response.close();
			} );
	}
//
//	public CompletableFuture<PersistedDocument> saveAndRetrieve( PersistedDocument document ) {
//		return this
//			.save( document )
//			.thenCompose( aVoid -> get( document.getIRI() ) );
//	}
//
//	public CompletableFuture<Void> delete( PersistedDocument document ) {
//		return delete( document.getIRI() );
//	}
//
//	public CompletableFuture<Void> delete( IRI documentIRI ) {
//		BoundRequestBuilder request = this.httpClient.prepareDelete( documentIRI.toString() );
//
//		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.RDF_SOURCE );
//
//		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );
//
//		return request
//			.execute().toCompletableFuture()
//			.thenAccept( response -> {
//				verifyResponseStatusCode( response,
//					HttpResponseStatus.OK.code(),
//					HttpResponseStatus.NO_CONTENT.code()
//				);
//			} );
//	}

	private HTTPClient.HTTPResponse verifyResponseStatusCode( HTTPClient.HTTPResponse response, int... expectedStatusCodes ) {
		int code = response.getResponseCode();
		for ( int expectedStatusCode : expectedStatusCodes ) {
			if ( expectedStatusCode == code ) return response;
		}

		if ( code >= 400 ) throwHTTPException( response );

		return response;
	}

	private void throwHTTPException( HTTPClient.HTTPResponse response ) {
		int code = response.getResponseCode();

		AbstractModel errorObject = null;
		if ( "application/ld+json".equals( response.getHeader( "content-type" ) ) ) {
			try {
				errorObject = JSONLDParser.parse( response.getBody() );
			} catch ( Exception e ) {
				// TODO: Instead of swallowing the exception, log it and continue with the execution
			}
		}

		throwHTTPException( code, errorObject );
	}

	private void throwHTTPException( int code, AbstractModel errorObject ) {
		HTTPClient.StatusCode statusCode = HTTPClient.StatusCode.valueOf( code );

		// TODO: Add missing error codes
		if ( statusCode == null ) {
			throw new RuntimeException( "Status code: '" + code + "', was not expected" );
		} else if ( statusCode.equals( HTTPClient.StatusCode.BAD_REQUEST ) ) {
			throw new BadRequestException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.UNAUTHORIZED ) ) {
			throw new UnauthorizedException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.FORBIDDEN ) ) {
			throw new ForbiddenException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.NOT_FOUND ) ) {
			throw new NotFoundException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.METHOD_NOT_ALLOWED ) ) {
			throw new MethodNotAllowedException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.NOT_ACCEPTABLE ) ) {
			throw new NotAcceptableException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.PROXY_AUTHENTICATION_REQUIRED ) ) {
			throw new ProxyAuthenticationRequiredException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.REQUEST_TIMEOUT ) ) {
			throw new RequestTimeoutException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.CONFLICT ) ) {
			throw new ConflictException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.GONE ) ) {
			throw new GoneException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.LENGTH_REQUIRED ) ) {
			throw new LengthRequiredException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.PRECONDITION_FAILED ) ) {
			throw new PreconditionFailedException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.REQUEST_ENTITY_TOO_LARGE ) ) {
			throw new RequestEntityTooLargeException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.REQUEST_URI_TOO_LONG ) ) {
			throw new RequestURITooLongException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.UNSUPPORTED_MEDIA_TYPE ) ) {
			throw new UnsupportedMediaTypeException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE ) ) {
			throw new RequestedRangeNotSatisfiableException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.EXPECTATION_FAILED ) ) {
			throw new ExpectationFailedException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.INTERNAL_SERVER_ERROR ) ) {
			throw new InternalServerErrorException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.NOT_IMPLEMENTED ) ) {
			throw new NotImplementedException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.BAD_GATEWAY ) ) {
			throw new BadGatewayException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.SERVICE_UNAVAILABLE ) ) {
			throw new ServiceUnavailableException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.GATEWAY_TIMEOUT ) ) {
			throw new GatewayTimeoutException( errorObject );
		} else if ( statusCode.equals( HTTPClient.StatusCode.HTTP_VERSION_NOT_SUPPORTED ) ) {
			throw new HTTPVersionNotSupportedException( errorObject );
		} else {
			throw new HTTPResponseException( code, errorObject ) {
				@Override
				public int getStatusCode() {
					return code;
				}

				@Override
				public Model getErrorObject() {
					return errorObject;
				}
			};
		}
	}
}
