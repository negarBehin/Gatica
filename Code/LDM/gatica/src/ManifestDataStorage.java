import java.net.InetAddress;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import sedona.Facets;
import sedona.Schema;
import sedona.Type;
import sedona.Value;
import sedona.dasp.DaspSocket;
import sedona.manifest.SlotManifest;

import sedona.sox.SoxClient;



public class ManifestDataStorage
{

	///////////////////////////////////////////////////////////////////////////////////
    // Required URIs 
	///////////////////////////////////////////////////////////////////////////////////
	
	public static final String baseURI			= "http://www.gatica.org/ontologies/ontology";
	public static final String sensorUIR		= "http://www.gatica.org/ontologies/sensor:";
	public static final String patientURI		= "http://www.gatica.org/ontologies/patient:";
	public static final String dulURI			= "http://www.loa-cnr.it/ontologies/DUL.owl#";
	public static final String ssnURI			= "http://purl.oclc.org/NET/ssnx/ssn#";
	public static final String rdfURI			= "http://www.w3.org/2000/01/rdf-schema";
	
	public static final String tdbDirectory		= "TDB";
	
	public static void GetManifestInfo()
	{
		int				i;
		String			user 		= "admin";
		String			password 	= "";
		String			sedonaHome	= "/home/negar/sedonadev/";
		String			address		= "127.0.0.1";
		int				soxPort		= 1876;
		DaspSocket      daspSocket 	= null;
		SoxClient		soxClient 	= null;

      try
      {
    	// Setting sedona home property
          System.setProperty( "sedona.home" , sedonaHome );
          
          // Creating daspSocket object
          daspSocket 				= DaspSocket.open(-1 , null , DaspSocket.SESSION_QUEUING);
          InetAddress ina 			= InetAddress.getByName(address);
          
          // Creating soxClient object with required information
          soxClient 				= new SoxClient(daspSocket, ina , soxPort , user , password);
          
          // Connecting to Sedona device
          soxClient.connect();

          // reading Schema
          Schema schema 			= soxClient.readSchema();
          Type tc 					= schema.type("wfdb::Merge");
          
          // Getting manifest slots
          SlotManifest[] slots 		= tc.manifest.slots;
          String svmId				= tc.facet("svmID").toString();

          // Creating a connection to our dataset
          Dataset dataset = TDBFactory.createDataset( tdbDirectory );
  		
          // Begin a transaction in WRITE mode
          dataset.begin( ReadWrite.WRITE );
  		
          // Getting the default model of dataset
          Model model 	= dataset.getDefaultModel();
  		
          // Getting slots id, type and facets
          for (i = 0 ; i < tc.manifest.slots.length ; ++i){
        	  Integer slotId 		= slots[i].declaredId;
        	  //String slotType 		= slots[i].type;

        	  // Adding Slot instance to db
        	  String slotInstance	= "slot-" + svmId + "-" + slotId;
        	  Resource slotResorce 	= model.createResource( baseURI + ":" + slotInstance );
        	  Resource slotClassResource	= model.getResource( sensorUIR + "Slot" );
        	  model.add( slotResorce, RDF.type, slotClassResource );
        	  
        	  addStringDataProperty( 	model, slotResorce, 
        			  baseURI + ":hasId",
        			  slotId.toString() );
        	  model.add(	model.getResource( baseURI +":SVM-" + svmId ), 
        	  				model.getProperty( ssnURI + "hasSubSystem" ), 
        	  				slotResorce	);
        		  
        	  if (!slots[i].facets.isEmpty()){
  				Facets facets 	= slots[i].facets;

  				// Adding label
  				Value slotLabel 		= facets.get("label");
  				model.add( slotResorce, RDFS.label, slotLabel.toString() );

  				// Adding min
				Value slotMinVal 		= facets.get("min");
				Resource minResorce 	= model.createResource( baseURI + ":" + "Min-" + svmId + "-" + slotId );
				model.add( 	minResorce, RDF.type, model.getResource( sensorUIR + "Min" ));
				model.add( 	minResorce, model.getProperty( dulURI + "hasDataValue" ), slotMinVal.toString());
				model.add( 	slotResorce, model.getProperty( ssnURI +"hasProperty" ), minResorce);
						
				// Adding max
				Value slotMaxVal 		= facets.get("max");
				Resource maxResorce 	= model.createResource( baseURI + ":" + "Max-" + svmId + "-" + slotId );
				model.add( 	maxResorce, RDF.type, model.getResource( sensorUIR + "Max" ));
				model.add( 	maxResorce, model.getProperty( dulURI + "hasDataValue" ), slotMaxVal.toString());
				model.add( 	slotResorce, model.getProperty( ssnURI +"hasProperty" ), maxResorce);
				
				// Adding unit
				Value slotUnit 	= facets.get("unit");
				Resource unitResorce 	= model.createResource( baseURI + ":" + slotUnit );
				model.add( 	unitResorce, RDF.type, model.getResource( dulURI + "UnitOfMeasure" ));
				model.add( 	slotResorce, model.getProperty( dulURI + "isClassifiedBy" ), unitResorce );	  
            	}        		  
          }
          // Commit the changes
          dataset.commit();
			
          // End the transaction
          dataset.end();
			
          // Closing the model and dataset
          model.close();
          dataset.close();
          
          // Closing the soxClient
          soxClient.close();
          
          
      } catch (Exception e) {
    	  System.out.println(e);
      }
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	//addStringDataProperty method
	///////////////////////////////////////////////////////////////////////////////////

	private static void addStringDataProperty( 	Model model,
												Resource resource,
												String propertyURI,
												String value ) 
	{
		Property property = model.createProperty( propertyURI );
		resource.addProperty( property, value );
	}
	

	///////////////////////////////////////////////////////////////////////////////////
	//Main method
	///////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args){
    
	  GetManifestInfo();
	  System.out.println("Writting manifest data to tdb has been completed successfully.");
	}
  

}