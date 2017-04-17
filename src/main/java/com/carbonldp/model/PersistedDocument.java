package com.carbonldp.model;

import com.carbonldp.models.Document;
import com.carbonldp.rdf.RDFDocument;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.AbstractModel;

/**
 * @author MiguelAraCo
 */
public class PersistedDocument extends Document {
	String eTag;

	public PersistedDocument( RDFDocument rdfDocument ) {
		super( rdfDocument );
	}

	public PersistedDocument( IRI subject ) {
		super( subject );
	}

	public PersistedDocument( AbstractModel base, IRI subject ) {
		super( base, subject );
	}

	public String getETag() {
		return eTag;
	}

	public void setETag( String eTag ) {
		this.eTag = eTag;
	}
}
