package com.carbonldp;

import com.carbonldp.auth.AuthService;
import com.carbonldp.utils.IRIUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * @author MiguelAraCo
 */
public interface Context {
	Context getParentContext();

	default ValueFactory getValueFactory() {
		return this.getParentContext().getValueFactory();
	}

	String getBase();

	default IRI resolve( String iri ) {
		return IRIUtil.resolve( this.getBase(), iri, this.getValueFactory() );
	}

	DocumentService getDocumentService();

	AuthService getAuthService();
}
