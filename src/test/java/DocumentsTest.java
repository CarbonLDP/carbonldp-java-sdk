import com.carbonldp.Carbon;
import com.carbonldp.apps.AppContext;
import com.carbonldp.models.Document;
import com.carbonldp.rdf.EmptyIRI;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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
		this.carbon = new Carbon( true, "" );
		this.appContext = this.carbon.getAppContext( "" );
		this.appContext.getAuthService().authenticate( "", "" );
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

		this.appContext.getDocumentService()
		               .createChild( this.appContext.getRoot(), document )
		               .thenCompose( documentIRI -> {
			               Assert.assertNotNull( documentIRI );
			               return this.appContext.getDocumentService().get( documentIRI );
		               } )
		               .thenAccept( createdDocument -> {
			               Assert.assertEquals( createdDocument.getString( STRING_PROPERTY ), "hello world!" );
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
