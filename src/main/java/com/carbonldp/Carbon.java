package com.carbonldp;

import com.carbonldp.apps.AppContext;
import com.carbonldp.auth.AuthService;
import com.carbonldp.utils.IRIUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * @author MiguelAraCo
 */
public class Carbon implements Context {
	private static final String PLATFORM = "platform/";
	private static final String APPS_CONTAINER = "apps/";
	private static final String APPS_ROOT = "apps/";

	private final String protocol;
	private final String host;
	private final String base;

	private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
	private final DocumentService documentService;
	private final AuthService authService;

	public Carbon( boolean ssl, String host ) {
		this.protocol = ssl ? "https" : "http";

		if ( host == null ) throw new IllegalArgumentException( "host can't be null" );
		this.host = host.endsWith( "/" ) ? host : host + "/";

		this.base = protocol + "://" + host + PLATFORM;

		this.documentService = new DocumentService( this );
		this.authService = new AuthService( this );
	}

	@Override
	public Context getParentContext() {
		return null;
	}

	@Override
	public ValueFactory getValueFactory() {
		return this.valueFactory;
	}

	@Override
	public String getBase() {
		return this.base;
	}

	@Override
	public DocumentService getDocumentService() {
		return this.documentService;
	}

	@Override
	public AuthService getAuthService() {
		return this.authService;
	}

	public AppContext getAppContext( String appSlug ) {
		if ( ! IRIUtil.isAbsolute( appSlug ) ) {
			appSlug = appSlug.startsWith( "/" ) ? appSlug.substring( 1 ) : appSlug;
		}

		IRI appIRI = this.resolve( APPS_CONTAINER + appSlug );
		IRI appRootIRI = this.getValueFactory().createIRI( this.protocol + "://" + this.host + APPS_ROOT + appSlug );

		return new AppContext( this, appIRI, appRootIRI );
	}
}
