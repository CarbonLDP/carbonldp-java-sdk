package com.carbonldp;

import com.carbonldp.utils.AsyncHTTPUtils;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.AbstractModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * @author MiguelAraCo
 */
public class JSONLDParser implements Function<Response, HTTPResult<AbstractModel>> {
	public static void write( BoundRequestBuilder request, AbstractModel model ) {
		AsyncHTTPUtils.setContentTypeHeader( request );

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		RDFWriter writer = Rio.createWriter( RDFFormat.JSONLD, outputStream );
		try {
			writer.startRDF();
			for ( Statement statement : model ) {
				writer.handleStatement( statement );
			}
			writer.endRDF();
		} catch ( RDFHandlerException e ) {
			throw new HttpMessageNotWritableException( "The RDF model couldn't be wrote to an RDF document.", e );
		}

		request.setBody( outputStream.toByteArray() );
	}

	public static AbstractModel parse( InputStream inputStream ) {
		RDFParser parser = Rio.createParser( RDFFormat.JSONLD );

		AbstractModel model = new LinkedHashModel();

		parser.setRDFHandler( new StatementCollector( model ) );

		try {
			parser.parse( inputStream, "" );
		} catch ( RDFParseException | RDFHandlerException | IOException e ) {
			// TODO: Change me
			throw new RuntimeException( e );
		}

		return model;
	}

	@Override
	public HTTPResult<AbstractModel> apply( Response response ) {
		String contentType = response.getContentType();

		// TODO: Process mime types correctly
		// TODO: Throw appropriate exception
		if ( ! RDFFormat.JSONLD.getDefaultMIMEType().equals( contentType ) ) throw new RuntimeException( "The returned Content-Type: '" + contentType + "', is not supported" );

		InputStream bodyInputStream = response.getResponseBodyAsStream();

		AbstractModel model = parse( bodyInputStream );

		return new HTTPResult<>( response, model );
	}
}
