package com.carbonldp;

import com.carbonldp.http.HTTPClient;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.AbstractModel;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParserFactory;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author MiguelAraCo
 */
public class JSONLD {
	public JSONLD() {
		registerParserAndWriter();
	}

	public static void registerParserAndWriter() {
		// Sometimes the class loader doesn't register the JSONLDParserFactory, this forces it
		// TODO: Find out why and remove this
		if ( ! RDFParserRegistry.getInstance().has( RDFFormat.JSONLD ) ) {
			RDFParserRegistry.getInstance().add( new JSONLDParserFactory() );
		}
		if ( ! RDFWriterRegistry.getInstance().has( RDFFormat.JSONLD ) ) {
			RDFWriterRegistry.getInstance().add( new JSONLDWriterFactory() );
		}
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

	public static class NativeParser implements Function<HTTPClient.OpenHTTPResponse, AbstractModel> {
		public NativeParser() {
			registerParserAndWriter();
		}

		@Override
		public AbstractModel apply( HTTPClient.OpenHTTPResponse response ) {
			String contentType = response.getHeader( "content-type" );

			// TODO: Process mime types correctly
			// TODO: Throw appropriate exception
			if ( ! RDFFormat.JSONLD.getDefaultMIMEType().equals( contentType ) ) throw new RuntimeException( "The returned Content-Type: '" + contentType + "', is not supported" );

			InputStream bodyInputStream = response.getBody();

			return parse( bodyInputStream );
		}
	}

	public static class NativeWriter implements Consumer<OutputStream> {
		public static void setContentType( HTTPClient.HTTPRequest request ) {
			request.header( "Content-Type", "application/ld+json" );
		}

		private final AbstractModel model;

		public NativeWriter( AbstractModel model ) {
			this.model = model;
		}

		@Override
		public void accept( OutputStream outputStream ) {
			RDFWriter writer = Rio.createWriter( RDFFormat.JSONLD, outputStream );
			try {
				writer.startRDF();
				for ( Statement statement : model ) {
					writer.handleStatement( statement );
				}
				writer.endRDF();
			} catch ( RDFHandlerException e ) {
				throw new RuntimeException( "The RDF model couldn't be wrote to an RDF document.", e );
			}
		}
	}
}
