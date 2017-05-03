package com.carbonldp;

import com.carbonldp.descriptions.APIPreferences;
import com.carbonldp.exceptions.*;
import com.carbonldp.ldp.containers.MembersActionDescription;
import com.carbonldp.model.PersistedDocument;
import com.carbonldp.models.Document;
import com.carbonldp.models.Fragment;
import com.carbonldp.rdf.EmptyIRI;
import com.carbonldp.utils.AsyncHTTPUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.AbstractModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author MiguelAraCo
 */
public class DocumentService {
	private final Context context;
	private final AsyncHttpClient httpClient = new DefaultAsyncHttpClient();

	public DocumentService( Context context ) {
		this.context = context;
	}

	public CompletableFuture<PersistedDocument> get( String documentIRI ) {
		return get( this.context.resolve( documentIRI ) );
	}

	public CompletableFuture<PersistedDocument> get( IRI documentIRI ) {
		BoundRequestBuilder request = this.httpClient.prepareGet( documentIRI.toString() );

		AsyncHTTPUtils.setAcceptHeader( request );
		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.RDF_SOURCE );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		return request
			.execute().toCompletableFuture()
			.thenApply( response -> {
				verifyResponseStatusCode( response,
					HttpResponseStatus.OK.code()
				);

				return response;
			} )
			.thenApply( new JSONLDParser() )
			.thenApply( result -> {
				String location = result.response.getHeader( "content-location" );
				if ( location == null ) {
					// TODO
				} else {
					// TODO
				}

				PersistedDocument document = new PersistedDocument( result.body, documentIRI );

				Optional<String> eTag = AsyncHTTPUtils.getETag( result.response );
				if ( ! eTag.isPresent() ) throw new BadResponseException( "The response didn't contained an ETag" );

				document.setETag( eTag.get() );

				return document;
			} );
	}

	public CompletableFuture<Void> addMember( IRI document, IRI member ) {
		return addMembers( document, Arrays.asList( member ) );
	}

	public CompletableFuture<Void> addMembers( IRI document, Collection<IRI> members ) {
		Document addActionDocument = new Document( new EmptyIRI() );

		Fragment addAction = new Fragment( SimpleValueFactory.getInstance().createBNode(), addActionDocument );
		addAction.addType( MembersActionDescription.Resource.ADD.getIRI() );
		addAction.add( MembersActionDescription.Property.TARGET_MEMBER.getIRI(), members );

		BoundRequestBuilder request = this.httpClient.preparePut( document.toString() );

		AsyncHTTPUtils.setContentTypeHeader( request );
		AsyncHTTPUtils.setAcceptHeader( request );
		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.CONTAINER );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		// TODO: Use DocumentParser instead of using BaseModel
		JSONLDParser.write( request, addActionDocument.getBaseModel() );

		// TODO: Abstract response handling
		return request
			.execute().toCompletableFuture()
			.thenAccept( response -> {
				verifyResponseStatusCode( response,
					HttpResponseStatus.OK.code(),
					HttpResponseStatus.NO_CONTENT.code()
				);
			} );
	}

	public CompletableFuture<IRI> createChild( Document parentDocument, Document childDocument ) {
		return createChild( parentDocument, childDocument, null );
	}

	public CompletableFuture<IRI> createChild( Document parentDocument, Document childDocument, String slug ) {
		return createChild( parentDocument.getIRI(), childDocument, slug );
	}

	public CompletableFuture<IRI> createChild( String parentDocument, Document childDocument ) {
		return createChild( parentDocument, childDocument, null );
	}

	public CompletableFuture<IRI> createChild( String parentDocument, Document childDocument, String slug ) {
		return createChild( this.context.resolve( parentDocument ), childDocument, slug );
	}

	public CompletableFuture<IRI> createChild( IRI parentDocument, Document childDocument ) {
		return createChild( parentDocument, childDocument, null );
	}

	public CompletableFuture<IRI> createChild( IRI parentDocument, Document childDocument, String slug ) {
		return createDocument( parentDocument, childDocument, slug, APIPreferences.InteractionModel.CONTAINER );
	}

	public CompletableFuture<IRI> createAccessPoint( IRI parentDocument, Document childDocument, String slug ) {
		return createDocument( parentDocument, childDocument, slug, APIPreferences.InteractionModel.RDF_SOURCE );
	}

	private CompletableFuture<IRI> createDocument( IRI parentDocument, Document childDocument, String slug, APIPreferences.InteractionModel interactionModel ) {
		BoundRequestBuilder request = this.httpClient.preparePost( parentDocument.toString() );

		AsyncHTTPUtils.setContentTypeHeader( request );
		AsyncHTTPUtils.setInteractionModel( request, interactionModel );

		if ( slug != null ) AsyncHTTPUtils.setSlug( request, slug );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		// TODO: Use DocumentParser instead of using BaseModel
		JSONLDParser.write( request, childDocument.getBaseModel() );

		return request
			.execute().toCompletableFuture()
			.thenApply( response -> {
				verifyResponseStatusCode( response,
					HttpResponseStatus.CREATED.code(),
					HttpResponseStatus.NO_CONTENT.code()
				);

				Optional<String> location = AsyncHTTPUtils.getHeader( "Location", response );
				if ( ! location.isPresent() ) throw new BadResponseException( "The response didn't include a location header" );

				return this.context.getValueFactory().createIRI( location.get() );
			} );
	}

	public CompletableFuture<Void> save( PersistedDocument document ) {
		BoundRequestBuilder request = this.httpClient.preparePut( document.getIRI().toString() );

		AsyncHTTPUtils.setAcceptHeader( request );
		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.RDF_SOURCE );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		AsyncHTTPUtils.setIfMatch( request, document.getETag() );

		// TODO: Use DocumentParser instead of using BaseModel
		JSONLDParser.write( request, document.getBaseModel() );

		return request
			.execute().toCompletableFuture()
			.thenAccept( response -> {
				verifyResponseStatusCode( response,
					HttpResponseStatus.OK.code(),
					HttpResponseStatus.NO_CONTENT.code()
				);
			} );
	}

	public CompletableFuture<PersistedDocument> saveAndRetrieve( PersistedDocument document ) {
		return this
			.save( document )
			.thenCompose( aVoid -> get( document.getIRI() ) );
	}

	public CompletableFuture<Void> delete( PersistedDocument document ) {
		return delete( document.getIRI() );
	}

	public CompletableFuture<Void> delete( IRI documentIRI ) {
		BoundRequestBuilder request = this.httpClient.prepareDelete( documentIRI.toString() );

		AsyncHTTPUtils.setInteractionModel( request, APIPreferences.InteractionModel.RDF_SOURCE );

		if ( this.context.getAuthService().isAuthenticated() ) this.context.getAuthService().addAuthenticationHeaders( request );

		return request
			.execute().toCompletableFuture()
			.thenAccept( response -> {
				verifyResponseStatusCode( response,
					HttpResponseStatus.OK.code(),
					HttpResponseStatus.NO_CONTENT.code()
				);
			} );
	}

	private void verifyResponseStatusCode( Response response, int... expectedStatusCodes ) {
		int code = response.getStatusCode();
		for ( int expectedStatusCode : expectedStatusCodes ) {
			if ( expectedStatusCode == code ) return;
		}

		if ( code >= 400 ) throwHTTPException( response );
	}

	private void throwHTTPException( Response response ) {
		int code = response.getStatusCode();

		AbstractModel errorObject = null;
		if ( "application/ld+json".equals( response.getContentType().toLowerCase() ) ) {
			try {
				errorObject = JSONLDParser.parse( response.getResponseBodyAsStream() );
			} catch ( Exception e ) {
				// TODO: Instead of swallowing the exception, log it and continue with the execution
			}
		}

		HttpResponseStatus statusCode = HttpResponseStatus.valueOf( code );

		// TODO: Add missing error codes
		if ( statusCode == null ) {
			throw new RuntimeException( "Status code: '" + code + "', was not expected" );
		} else if ( statusCode.equals( HttpResponseStatus.BAD_REQUEST ) ) {
			throw new BadRequestException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.UNAUTHORIZED ) ) {
			throw new UnauthorizedException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.FORBIDDEN ) ) {
			throw new ForbiddenException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.NOT_FOUND ) ) {
			throw new NotFoundException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.METHOD_NOT_ALLOWED ) ) {
			throw new MethodNotAllowedException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.NOT_ACCEPTABLE ) ) {
			throw new NotAcceptableException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED ) ) {
			throw new ProxyAuthenticationRequiredException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.REQUEST_TIMEOUT ) ) {
			throw new RequestTimeoutException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.CONFLICT ) ) {
			throw new ConflictException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.GONE ) ) {
			throw new GoneException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.LENGTH_REQUIRED ) ) {
			throw new LengthRequiredException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.PRECONDITION_FAILED ) ) {
			throw new PreconditionFailedException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE ) ) {
			throw new RequestEntityTooLargeException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.REQUEST_URI_TOO_LONG ) ) {
			throw new RequestURITooLongException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE ) ) {
			throw new UnsupportedMediaTypeException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE ) ) {
			throw new RequestedRangeNotSatisfiableException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.EXPECTATION_FAILED ) ) {
			throw new ExpectationFailedException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.INTERNAL_SERVER_ERROR ) ) {
			throw new InternalServerErrorException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.NOT_IMPLEMENTED ) ) {
			throw new NotImplementedException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.BAD_GATEWAY ) ) {
			throw new BadGatewayException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.SERVICE_UNAVAILABLE ) ) {
			throw new ServiceUnavailableException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.GATEWAY_TIMEOUT ) ) {
			throw new GatewayTimeoutException( errorObject );
		} else if ( statusCode.equals( HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED ) ) {
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
