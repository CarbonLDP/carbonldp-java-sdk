package com.carbonldp.model;

import com.carbonldp.models.Document;
import com.carbonldp.models.DocumentGraph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.AbstractModel;

/**
 * @author MiguelAraCo
 */
public class PersistedDocument extends Document {
	String eTag;

	public PersistedDocument( Document document ) {
		super( document );
	}

	public PersistedDocument( DocumentGraph documentGraph ) {
		super( documentGraph );
	}

	public PersistedDocument( IRI subject ) {
		super( subject );
	}

	public PersistedDocument( AbstractModel base, IRI subject ) {
		super( base, subject );
	}

	public String getETag() {
		return this.eTag;
	}

	public void setETag( String eTag ) {
		this.eTag = eTag;
	}
}
