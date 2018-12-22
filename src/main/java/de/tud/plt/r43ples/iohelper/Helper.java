package de.tud.plt.r43ples.iohelper;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.optimization.ChangeSetPath;
import de.tud.plt.r43ples.optimization.PathCalculationFabric;
import de.tud.plt.r43ples.optimization.PathCalculationInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;

/**
 * This class provides several helpful methods.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
public class Helper {

	/** The logger. **/
	private static Logger logger = LogManager.getLogger(Helper.class);


	/**
	 * Generates difference between revision a and revision b of a named graph.
	 * Revision a is considered older than revision b (a -> b)
	 * @param graphName name of graph
	 * @param revAIdentifier identifier revision a
	 * @param revBIdentifier identifier revision b
	 * @param fileFormat format of outputfile. possible options: nquads, trig
	 * @return list of diffs
	 */
	public static String getDiffsBetweenStartAndTargetRevision(
			final String graphName,
			final String revAIdentifier,
			final String revBIdentifier,
			final String fileFormat) {

		// create revision graph
		RevisionGraph graph = new RevisionGraph(graphName);
		PathCalculationInterface calculation = PathCalculationFabric.getInstance(graph);

		// get revisions
		Revision revA = null;
		Revision revB = null;

		try {
			revA = new Revision(graph, revAIdentifier, true);
		} catch (InternalErrorException e) {
			e.printStackTrace();
			return "Error finding revision A:\n" + e.getMessage();
		}

		try {
			revB = new Revision(graph, revBIdentifier, true);
		} catch (InternalErrorException e) {
			e.printStackTrace();
			return "Error finding revision B:\n" + e.getMessage();
		}

		// get changeset path
		ChangeSetPath path = null;
		try {
			path = calculation.getChangeSetsBetweenStartAndTargetRevision(revA, revB);
		} catch (InternalErrorException e) {
			e.printStackTrace();
			return "Error creating changeset. Make sure revision a lies before revision b:\n" + e.getMessage();
		}

		// process changeset to get diffs

		LinkedList<ChangeSet> changeSets = path.getRevisionPath();

		// first changeset as base
		// ! changeSets starts with last changeset (inverse)
		ChangeSet oldCS = changeSets.get(changeSets.size() - 1);

		Model oldAddModel = JenaModelManagement.readStringToJenaModel(oldCS.getAddSetContent(), "TURTLE");
		Model oldDeleteModel = JenaModelManagement.readStringToJenaModel(oldCS.getDeleteSetContent(), "TURTLE");

		// generate diff model (cross compare add and delete sets)
		for (int i = changeSets.size() - 2; i >= 0; i--) {

			ChangeSet newCS = changeSets.get(i);

			Model addModel = JenaModelManagement.readStringToJenaModel(newCS.getAddSetContent(), "TURTLE");
			Model deleteModel = JenaModelManagement.readStringToJenaModel(newCS.getDeleteSetContent(), "TURTLE");

			// find neutralizing triples and delete them
			Model neutralAdd = oldDeleteModel.intersection(addModel);
			Model neutralDelete = oldAddModel.intersection(deleteModel);

			Model test = oldDeleteModel.remove(neutralAdd);
			addModel.remove(neutralAdd);
			oldAddModel.remove(neutralDelete);
			deleteModel.remove(neutralDelete);

			// add remaining triples of new model to old model
			oldDeleteModel.add(deleteModel);
			oldAddModel.add(addModel);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// create dataset from add and delete model for transmission
		Dataset resultSet = DatasetFactory.create();
		resultSet.addNamedModel("AddSet", oldAddModel);
		resultSet.addNamedModel("DeleteSet", oldDeleteModel);

		// create returnable result string
		if (fileFormat.toLowerCase().contains("trig"))
			RDFDataMgr.write(baos, resultSet, RDFFormat.TRIG_PRETTY);
		else if (fileFormat.toLowerCase().contains("nquads"))
			RDFDataMgr.write(baos, resultSet, RDFFormat.NQUADS_UTF8);
		else {
			return "unsupported file format";
		}

		return baos.toString();
	}


	/**
	 * Split huge INSERT statements into separate queries of up to 500 triple
	 * statements.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param dataSetAsNTriples
	 *            the data to insert as N-Triples
	 */
	public static void executeINSERT(final String graphName, final String dataSetAsNTriples) {

		String insertQueryTemplate = "INSERT DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, insertQueryTemplate);
	}
	
	/**
	 * Split huge DELETE statements into separate queries of up to fifty triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples 
	 */
	public static void executeDELETE(final String graphName, final String dataSetAsNTriples) {

		String deleteQueryTemplate = "DELETE DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, deleteQueryTemplate);
	}
	
	
	
	public static void splitAndExecuteBigQuery(final String graphName, final String dataSetAsNTriples, final String template){
		final int MAX_STATEMENTS = 500;
		String[] lines = dataSetAsNTriples.split("\n");
		int counter = 0;
		StringBuilder insert = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			insert.append(lines[i]);
			insert.append("\n");
			counter++;
			if (counter == MAX_STATEMENTS-1) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
				counter = 0;
				insert = new StringBuilder();
			}
		}

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
	}

	
	/**
	 * Get the content of this revision graph by execution of CONSTRUCT.
	 * 
	 * @param graphName the graphName
	 * @param format RDF serialization format which should be returned
	 * @return the constructed graph content as specified RDF serialization format
	 */
	public static String getContentOfGraph(final String graphName, final String format) {
		String query = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { GRAPH <%s> {?s ?p ?o} }", graphName);
		return TripleStoreInterfaceSingleton.get().executeConstructQuery(query, format);		
	}

	/**
	 * Get the content of a named graph as N-TRIPLES.
	 *
	 * @param namedGraphURI the named graph URI
	 * @return the content of the named graph as N-TRIPLES
	 */
	public static String getContentOfNamedGraphAsN3(String namedGraphURI) {
		String query = Config.prefixes + String.format(
				"CONSTRUCT {?s ?p ?o} %n"
						+ "WHERE { GRAPH <%s> {?s ?p ?o} }", namedGraphURI);
		String resultAsTurtle = TripleStoreInterfaceSingleton.get().executeConstructQuery(query, "TURTLE");
		return JenaModelManagement.convertJenaModelToNTriple(JenaModelManagement.readStringToJenaModel(resultAsTurtle, "TURTLE"));
	}

	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @param format
	 *            serialization of the response
	 * @return String containing the SPARQL response in specified format
	 */
	public static String getRevisedGraphsSparql(final String format) {
		String sparqlQuery = Config.prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> { ?graph a rmo:Graph. }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery, format);
	}

	/**
	 * Get the user URI. If the user does not exist then the user will be created.
	 *
	 * @param user name as string
	 * @return URI of user
	 */
	public static String getUserURI(final String user) {
		// When user does not already exists - create new

		String query = Config.prefixes
				+ String.format("SELECT ?personUri { GRAPH <%s>  { " + "?personUri a prov:Person;"
						+ "  rdfs:label \"%s\"." + "} }", Config.revision_graph, user); //TODO check if the users are created within the special revision graph - maybe move to RevisionGraph
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (results.hasNext()) {
			logger.debug("User " + user + " already exists.");
			QuerySolution qs = results.next();
			return qs.getResource("?personUri").toString();
		} else {
			String personUri = null;
			try {
				personUri = "http://eatld.et.tu-dresden.de/persons/" + URLEncoder.encode(user, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			logger.debug("User does not exists. Create user " + personUri + ".");
			query = Config.prefixes
					+ String.format("INSERT DATA { GRAPH <%s> { <%s> a prov:Person; rdfs:label \"%s\". } }",
							Config.revision_graph, personUri, user);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
			return personUri;
		}
	}


	public static String getSparqlQueryFromSpin(String spinString) {

		// Register system functions (such as sp:gt (>))
		SPINModuleRegistry.get().init();

		// Create an empty OntModel importing SP
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("ex", "http://example.org/demo#");

		String query =
				"SELECT ?person\n" +
						"WHERE {\n" +
						"    ?person a ex:Person .\n" +
						"    ?person ex:age ?age .\n" +
						"    FILTER (?age > 18) .\n" +
						"}";

		Query arqQuery = ARQFactory.get().createQuery(model, query);
		ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
		Select spinQuery = (Select) arq2SPIN.createQuery(arqQuery, null);

		System.out.println("SPIN query in Turtle:");
		model.write(System.out, FileUtils.langTurtle);

		System.out.println("-----");
		String str = spinQuery.toString();
		System.out.println("SPIN query:\n" + str);

		// Now turn it back into a Jena Query
		Query parsedBack = ARQFactory.get().createQuery(spinQuery);
		System.out.println("Jena query:\n" + parsedBack);


		return null;
	}

}

