package com.carbonldp;

import com.carbonldp.descriptions.APIPreferences;
import com.carbonldp.exceptions.BadResponseException;
import com.carbonldp.exceptions.PreconditionFailedException;
import com.carbonldp.ldp.containers.MembersActionDescription;
import com.carbonldp.model.PersistedDocument;
import com.carbonldp.models.Document;
import com.carbonldp.models.Fragment;
import com.carbonldp.rdf.EmptyIRI;
import com.carbonldp.utils.AsyncHTTPUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.http.HttpStatus;

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
				if ( response.getStatusCode() != 200 ) {
					// TODO
				}

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
			.thenApply( response -> {
				switch ( HttpStatus.valueOf( response.getStatusCode() ) ) {
					case OK:
					case NO_CONTENT:
						return null;
					default:
						// TODO
						throw new RuntimeException( "Status code: '" + response.getStatusCode() + "', was not expected" );
				}
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
				switch ( HttpStatus.valueOf( response.getStatusCode() ) ) {
					case CREATED:
					case NO_CONTENT:
						break;
					default:
						// TODO
						throw new RuntimeException( "Status code: '" + response.getStatusCode() + "', was not expected" );
				}

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
			.thenApply( response -> {
				switch ( HttpStatus.valueOf( response.getStatusCode() ) ) {
					case OK:
					case NO_CONTENT:
						return null;
					case PRECONDITION_FAILED:
						throw new PreconditionFailedException();
					default:
						// TODO
						throw new RuntimeException( "Status code: '" + response.getStatusCode() + "', was not expected" );
				}
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
				switch ( HttpStatus.valueOf( response.getStatusCode() ) ) {
					case OK:
					case NO_CONTENT:
						return;
					default:
						// TODO
						throw new RuntimeException( "Status code: '" + response.getStatusCode() + "', was not expected" );
				}
			} );
	}
}
