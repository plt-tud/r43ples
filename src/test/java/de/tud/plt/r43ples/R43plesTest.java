package de.tud.plt.r43ples;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.Endpoint;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class provides the basic methods for test execution.
 * Each test has to inherit from it.
 * 
 * @author Stephan Hensel
 * 
 */
public class R43plesTest {

	/** The logger. **/
	protected static Logger logger = Logger.getLogger(R43plesTest.class);
	/** The endpoint. **/
	protected final Endpoint ep = new Endpoint();

	/**
     * Checks the isomorphism between two sets of triples - both in turtle serialization
     *
     * @param result_set the result set
     * @param expected_set the expected set
     * @return true if the two models are isomorphic
     */
    public boolean check_isomorphism(String result_set, String expected_set) {
        return check_isomorphism(result_set, "TURTLE", expected_set, "TURTLE");
    }

    /**
     * Checks the isomorphism between two sets of triples with given specific serialisations.
     *
     * @param result_set          the result set
     * @param result_set_format   the result set format
     * @param expected_set        the expected set
     * @param expected_set_format the expected set format
	 * @return true if the two models are isomorphic
	 */
	public boolean check_isomorphism(String result_set, String result_set_format, String expected_set, String expected_set_format) {
		return check_isomorphism(JenaModelManagement.readStringToJenaModel(result_set, result_set_format), JenaModelManagement.readStringToJenaModel(expected_set, expected_set_format));
	}
	
		
	/**
	 * Checks the isomorphism between two models.
	 * 
	 * @param result_model the result model
	 * @param expected_model the expected model
	 * @return true if the two models are isomorphic
	 */
	public boolean check_isomorphism(Model result_model, Model expected_model) {
		// Check isomorphism
		if (result_model.isIsomorphicWith(expected_model)) {
			return true;
		} else {
			// Get the differences between the models

			// Resulting model contains all statements which are in the result_model but not in the expected_model		
            logger.error("The following statements are in the result_model but not in the expected_model: \n"
                    + JenaModelManagement.convertJenaModelToNTriple(result_model.difference(expected_model)));
			// Resulting model contains all statements which are in the expected_model but not in the result_model	
            logger.error("The following statements are in the expected_model but not in the result_model: \n"
                    + JenaModelManagement.convertJenaModelToNTriple(expected_model.difference(result_model)));

            return false;
		}
	}

    /**
     * Remove all statements with property prov:atTime from given model
     *
     * @param model model which should remove all prov:atTime statements
     * @return model with removed statements
     */
    public Model removeTimeStampFromModel(Model model) {
        // Remove timestamp for test
        Property provAtTime = model.getProperty("http://eatld.et.tu-dresden.de/rmo#atTime");
        StmtIterator stmtIterator = model.listStatements(null, provAtTime, (RDFNode) null);
        model.remove(stmtIterator);

        return model;
    }

	/**
	 * Checks if the result and the expected graphs are isomorphic by using assertion.
	 *
	 * @param result the result graph as turtle
	 * @param expected the expected graph as turtle
	 */
    public void assertIsomorphism(String result, String expected) {
		Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
		Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		this.removeTimeStampFromModel(model_result);
		this.removeTimeStampFromModel(model_expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));
	}


	/**
	 * Checks if the content of a graph is isomorphic to the expected by using assertion.
	 *
	 * @param graphURI the graph URI identifying the graph to check
	 * @param expected the expected graph as turtle
	 */
	public void assertContentOfGraph(String graphURI, String expected) {
		assertIsomorphism(RevisionManagementOriginal.getContentOfGraph(graphURI, "TURTLE"), expected);
	}


	/**
	 * Checks the existence of all necessary graphs by using assertion.
	 *
	 * @param list the list of graph URIs
	 */
	public void assertListAll(LinkedList<String> list, TripleStoreInterface tripleStoreInterface) {
		Iterator<String> iterator = tripleStoreInterface.getGraphs();
		while (iterator.hasNext()) {
			String next = iterator.next();
			if (list.contains(next)) {
				list.remove(next);
			} else {
				Assert.fail("The graph URI <" + next + "> is missing in the list of expected URIs.");
			}
		}
		if (!list.isEmpty()) {
			Assert.fail("The following URIs can not be found in triple store: " + list.toArray().toString());
		}
	}

}
