///////////////////////////////////////////////////////////////////////////////////////
// Importing required headers 
///////////////////////////////////////////////////////////////////////////////////////

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.tdb.TDBFactory;

///////////////////////////////////////////////////////////////////////////////////////
//
//Main Class : RetrievingTDBContents 
//
///////////////////////////////////////////////////////////////////////////////////////

public class RetrievingTDBContents {
	
	///////////////////////////////////////////////////////////////////////////////////
	//The tdb Directory 
	///////////////////////////////////////////////////////////////////////////////////
	public static final String tdbDirectory		= "TDB";

	
	public static void main(String[] args) {

		///////////////////////////////////////////////////////////////////////////////
		//Retrieving rdf data from tdb dataset
		///////////////////////////////////////////////////////////////////////////////

		// Creating a connection to our dataset
		Dataset dataset 		= TDBFactory.createDataset( tdbDirectory );
		Model model 			= dataset.getDefaultModel();
		
		// Begin a transaction in READ mode
		dataset.begin( ReadWrite.READ );
		
		// Getting all rdf statements in a list 
		StmtIterator iter		= model.listStatements();
		
	    // printing out the predicate, subject and object of each statement
	    while (iter.hasNext()) {
	        Statement stmt      = iter.nextStatement();
	        Resource  subject   = stmt.getSubject();
	        Property  predicate = stmt.getPredicate();
	        RDFNode   object    = stmt.getObject();
	        
	        // Printing the retrieved data
	        System.out.print( subject.toString() );
	        System.out.print( "\t" + predicate.toString() + "\t" );
	        
	        if (object instanceof Resource) {
	            System.out.print( object.toString() );
	        } 
	        else {
	            // object is a literal
	            System.out.print( " \"" + object.toString() + "\"" );
	        }
	        System.out.println(" .");
	    }
	    
	    // End the transaction
	    dataset.end();
	    
	    // Closing the model and dataset
	    model.close();
	    dataset.close();
  }
}