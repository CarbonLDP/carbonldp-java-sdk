import com.carbonldp.Carbon;
import com.carbonldp.apps.AppContext;
import com.carbonldp.models.Document;
import com.carbonldp.rdf.EmptyIRI;
import com.carbonldp.rdf.RDFMap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author MiguelAraCo
 */
@Test
public class DocumentsTest {
	private static final IRI STRING_PROPERTY = SimpleValueFactory.getInstance().createIRI( "http://example.com/ns#property1" );

	private Carbon carbon;
	private AppContext appContext;

	@BeforeSuite
	public void setupSuite() {
		this.carbon = new Carbon( true, "carbonldp.base22.io" );
		this.appContext = this.carbon.getAppContext( "test-app/" );
		this.appContext.getAuthService().authenticate( "admin@carbonldp.com", "hello" );
	}

	@Test
	public void testDocumentRetrieval() {
		this.carbon
			.getDocumentService()
			.get( "https://carbonldp.base22.io/platform/api/" )
			.thenAccept( document -> {
				Assert.assertEquals( document.getIRI(), SimpleValueFactory.getInstance().createIRI( "https://carbonldp.base22.io/platform/api/" ) );
			} )
			.exceptionally( e -> {
				e.printStackTrace();
				return null;
			} ).join();
	}

	@Test
	public void testDocumentCreation() {
		Document document = new Document( new EmptyIRI() );
		document.set( STRING_PROPERTY, "hello world!" );

		Resource mapSubject = SimpleValueFactory.getInstance().createBNode();
		RDFMap rdfMap = RDFMap.factory.create( mapSubject, document.getGraph() );
		rdfMap.put( SimpleValueFactory.getInstance().createLiteral( "Hello" ), SimpleValueFactory.getInstance().createIRI( "http://example.com/1" ) );

		Map<String, Resource> map = rdfMap.asMap( String.class, Resource.class );
		map.put( "World!", SimpleValueFactory.getInstance().createIRI( "http://example.com/2" ) );

		document.set( SimpleValueFactory.getInstance().createIRI( "http://example.com/ns#map" ), mapSubject );

		this.appContext.getDocumentService()
		               .createChild( this.appContext.getRoot(), document )
		               .thenCompose( documentIRI -> {
			               Assert.assertNotNull( documentIRI );
			               return this.appContext.getDocumentService().get( documentIRI );
		               } )
		               .thenAccept( createdDocument -> {
			               Assert.assertEquals( createdDocument.getString( STRING_PROPERTY ), "hello world!" );

			               Resource createdMapSubject = createdDocument.getResource( SimpleValueFactory.getInstance().createIRI( "http://example.com/ns#map" ) );
			               RDFMap createdRDFMap = new RDFMap( createdMapSubject, createdDocument.getGraph() );
			               Map<String, Resource> createdMap = createdRDFMap.asMap( String.class, Resource.class );
			               Assert.assertEquals( createdMap.size(), 2 );
			               Assert.assertEquals( createdMap.get( "Hello" ), SimpleValueFactory.getInstance().createIRI( "http://example.com/1" ) );
			               Assert.assertEquals( createdMap.get( "World!" ), SimpleValueFactory.getInstance().createIRI( "http://example.com/2" ) );
		               } )
		               .exceptionally( e -> {
			               e.printStackTrace();
			               Assert.fail();
			               return null;
		               } ).join();
		;
	}

	@Test
	public void testDocumentUpdate() {
		Document document = new Document( new EmptyIRI() );
		document.set( STRING_PROPERTY, "hello world!" );

		this.appContext.getDocumentService()
		               .createChild( this.appContext.getRoot(), document )
		               .thenCompose( documentIRI -> {
			               Assert.assertNotNull( documentIRI );
			               return this.appContext.getDocumentService().get( documentIRI );
		               } )
		               .thenCompose( createdDocument -> {
			               Assert.assertEquals( createdDocument.getString( STRING_PROPERTY ), "hello world!" );

			               createdDocument.set( STRING_PROPERTY, "good bye!" );

			               return this.appContext.getDocumentService().saveAndRetrieve( createdDocument );
		               } )
		               .thenAccept( modifiedDocument -> {
			               Assert.assertEquals( modifiedDocument.getString( STRING_PROPERTY ), "good bye!" );
		               } )
		               .exceptionally( e -> {
			               e.printStackTrace();
			               Assert.fail();
			               return null;
		               } ).join();
		;
	}

	@Test
	public void testDocumentDeletion() {
		Document document = new Document( new EmptyIRI() );
		document.set( STRING_PROPERTY, "hello world!" );

		this.appContext.getDocumentService()
		               .createChild( this.appContext.getRoot(), document )
		               .thenCompose( documentIRI -> {
			               Assert.assertNotNull( documentIRI );
			               return this.appContext.getDocumentService().get( documentIRI );
		               } )
		               .thenCompose( createdDocument -> {
			               Assert.assertEquals( createdDocument.getString( STRING_PROPERTY ), "hello world!" );

			               return this.appContext.getDocumentService().delete( createdDocument );
		               } )
		               .thenAccept( aVoid -> {

		               } )
		               .exceptionally( e -> {
			               e.printStackTrace();
			               Assert.fail();
			               return null;
		               } ).join();
		;
	}
}
