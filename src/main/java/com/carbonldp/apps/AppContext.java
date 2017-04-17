package com.carbonldp.apps;

import com.carbonldp.Carbon;
import com.carbonldp.Context;
import com.carbonldp.DocumentService;
import com.carbonldp.auth.AuthService;
import com.carbonldp.utils.Assert;
import org.eclipse.rdf4j.model.IRI;

/**
 * @author MiguelAraCo
 */
public class AppContext implements Context {
	public final Carbon carbon;

	private final IRI appIRI;
	private final IRI appRootIRI;
	private final String base;

	private final DocumentService documentService;
	private final AuthService authService;

	public AppContext( Carbon carbon, IRI appIRI, IRI appRootIRI ) {
		Assert.notNull( carbon, "carbon" );
		Assert.notNull( appIRI, "appIRI" );

		this.carbon = carbon;
		this.appIRI = appIRI;
		this.appRootIRI = appRootIRI;

		this.base = this.appRootIRI.toString();
		this.documentService = new DocumentService( this );
		this.authService = new AuthService( this );
	}

	public IRI getRoot() {
		return this.appRootIRI;
	}

	@Override
	public Context getParentContext() {
		return this.carbon;
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
}
