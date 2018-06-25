///////////////////////////////////////////////////////////////////////////////////////
// Importing required headers 
///////////////////////////////////////////////////////////////////////////////////////

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

///////////////////////////////////////////////////////////////////////////////////////
//
//Main Class : DeviceDataStorage 
//
///////////////////////////////////////////////////////////////////////////////////////

public class DeviceDataStorage {
	
	///////////////////////////////////////////////////////////////////////////////////
    // SAMPLE OF Data received from SVM
	///////////////////////////////////////////////////////////////////////////////////
	
	/*
	public static final String DataRecieved = 
				"01,0,17194702830000000,-1949;01,1,17194702830000000,-1951; + 01,2,17194702830000000,-1955;"+
				"01,3,17194702830000000,-869;01,4,17194702830000000,1023;01,5,17194702830000000,-1537;"+
				"01,6,17194702830000000,-784;01,7,17194702830000000,-258;" +
				"01,0,17194704830000000,-1888;01,1,17194704830000000,-1893;01,2,17194704830000000,-1899;"+
				"01,3,17194704830000000,-349;01,4,17194704830000000,2047;01,5,17194704830000000,-1537;"+
				"01,6,17194704830000000,-4;01,7,17194704830000000,845;"+
				"01,0,17194706830000000,-1896;01,1,17194706830000000,-1901;"+
				"01,2,17194706830000000,-1907;01,3,17194706830000000,-438;01,4,17194706830000000,2047;"+
				"01,5,17194706830000000,-1537;01,6,17194706830000000,-138;01,7,17194706830000000,657;";
	*/			
				
	///////////////////////////////////////////////////////////////////////////////////
	//The pattern of data received ( output pattern of SVM )
	///////////////////////////////////////////////////////////////////////////////////
	public static final String outPattern = "((?<svmId>\\w+),(?<slotId>\\w+),(?<time>\\d+),(?<data>-*\\d+);)"; 

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

	///////////////////////////////////////////////////////////////////////////////////
    // DDStorage method:  Get the data, parse it, extract required data and save them
	// in database
	//////////////////////////////////////////////////////////////////////////////////
	
	private void DDStorage(String dataRecieved){
		
		// Compile the pattern and match it with DataRecieved
		Pattern pattern 	= Pattern.compile( outPattern );
		Matcher matcher 	= pattern.matcher( dataRecieved );
		
		// Creating a connection to our dataset
		Dataset dataset 	= TDBFactory.createDataset( tdbDirectory );
		
		// Begin a transaction in WRITE mode
		dataset.begin( ReadWrite.WRITE );
		
		// Getting the default model of dataset
		Model model 		= dataset.getDefaultModel();
		
	    // Loop over each matcher substring of DataRecieved and add required data 
		// which contains of time of sampling and data of each input slot(sensor)
		while (matcher.find()) {
			
			// svmId of related SVM
	        String svmId 	= matcher.group( "svmId"	);
	        // id of each slot ( sensor )
	        String slotId 	= matcher.group( "slotId"	);
	        // time of sampling in integer
	        String time = matcher.group( "time"	);
	        // Sample data
	        String data = matcher.group( "data"	);

	        // Getting the resource of related slot
	        String slotsUIR			= baseURI + ":" + "slot-" + svmId + "-" + slotId;
			Resource slotResource	= model.getResource( slotsUIR );
			
			// Adding time and data to a blank node
			Resource blankNode		= model.createResource();
			model.add( blankNode,	RDF.type,	model.getResource(ssnURI+ "Observation" ));
			Property dataProperty	= model.getProperty( sensorUIR + "hasObservationResultValue" );
			Property timeProperty	= model.getProperty( sensorUIR + "observationResultTime" );
			blankNode.addProperty( dataProperty, data, XSDDatatype.XSDfloat );
			blankNode.addProperty( timeProperty, time, XSDDatatype.XSDint );
			
			// Adding the blank node to related slot
			Property property 		= model.getProperty( ssnURI + "observedBy" );
			blankNode.addProperty( property, slotResource );

	    }
		// Commit the changes
		dataset.commit();
		
		// Ending the transaction
		dataset.end();
		
		// Closing the model and dataset
		model.close();
		dataset.close();
	} 
	
	///////////////////////////////////////////////////////////////////////////////////
    // main method: get the received data from port 20000 and call the DDStorage
	// method to save it in TDB
	//////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws IOException {	
		
		
		 String reciveddata;
         ServerSocket serversocket 			= new ServerSocket(20000);
		 DeviceDataStorage devicedatastorage= new DeviceDataStorage();

         while(true)
         {
            Socket socket 					= serversocket.accept();
            BufferedReader bufferedReader 	=
               new BufferedReader(new InputStreamReader(socket.getInputStream()));
            reciveddata 					= bufferedReader.readLine();
            System.out.println("Received Data: " + reciveddata);			
			devicedatastorage.DDStorage( reciveddata );
			System.out.println("Writting Received Data in tdb has been completed successfully.");
         }
		
	}
}