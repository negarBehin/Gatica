///////////////////////////////////////////////////////////////////////////////////////
// Importing required headers 
///////////////////////////////////////////////////////////////////////////////////////

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

///////////////////////////////////////////////////////////////////////////////////////
//
//Main Class : CreateTDB 
//
///////////////////////////////////////////////////////////////////////////////////////

public class CreateTDB {

	///////////////////////////////////////////////////////////////////////////////////
	//The tdb Directory 
	///////////////////////////////////////////////////////////////////////////////////
	public static final String tdbDirectory		= "TDB";
	
	public static void main(String[] args) {

		///////////////////////////////////////////////////////////////////////////////
		//Saving ontology model in tdb dataset
		///////////////////////////////////////////////////////////////////////////////

		// Creating a connection to our dataset
		Dataset dataset			= TDBFactory.createDataset( tdbDirectory );

		// Begin a transaction in WRITE mode
		dataset.begin( ReadWrite.WRITE );
		
		// Getting the default model of dataset
		Model model 			= dataset.getDefaultModel();
		
		// Loading the Ontology file in tdb dataset
		String source			= "ontologyIns.owl";
		FileManager.get().readModel( model, source);
		
		// Commit the changes
		dataset.commit();
		
		// Ending the transaction
		dataset.end();
		
		// Closing the model and dataset
		model.close();
		dataset.close();
		
		System.out.println("Writting Ontology to tdb has been completed successfully.");
	}
}