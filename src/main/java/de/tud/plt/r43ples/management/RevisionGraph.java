/**
 * 
 */
package de.tud.plt.r43ples.management;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

/**
 * The class RevisionGraph provides functionality for one named graph which is managed by R43ples
 * @author Markus Graube
 *
 */
public class RevisionGraph {

	/** The logger. **/
	private Logger logger = Logger.getLogger(RevisionManagement.class);
	
	private String graphName;

	/**
	 * 
	 * @param graphName Uri of the named graph which R43ples manages
	 */
	public RevisionGraph(final String graphName){
		this.graphName = graphName;
	}
	
	
	
	/**
	 * Get the content of this revision graph by execution of CONSTRUCT.
	 * 
	 * @param graphName the graphName
	 * @param format RDF serialization format which should be returned
	 * @return the constructed graph content as specified RDF serialization format
	 */
	public String getContentOfRevisionGraph(final String format) {
		return RevisionManagement.getContentOfGraph(this.getRevisionGraphUri(), format);	
	}


	/** returns the name of the named graph which stores all revision information for the specified revised named graph
	 * 
	 * @return uri of the revision graph for this graph
	 */
	public String getRevisionGraphUri() {
		String query = String.format(
				  "SELECT ?revisionGraph "
				+ "WHERE { GRAPH <%s> {"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#hasRevisionGraph> ?revisionGraph ."
				+ "} }", Config.revision_graph, graphName);
			
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
			
			if (results.hasNext()) {
				QuerySolution qs = results.next();
				return qs.getResource("?revisionGraph").toString();
			} else {
				return null;
			}
	}

	/**
	 * Checks if the graph has a branch with the given identifier
	 * 
	 * @param identifier
	 *            revision number or branch or tag name of the graph
	 * @return true if specified revision of the graph is a branch
	 */
	public boolean hasBranch(final String identifier) {
		String revisionGraph = this.getRevisionGraphUri();
		String queryASK = Config.prefixes
				+ String.format(""
						+ "ASK { GRAPH <%s> { " 
						+ " ?rev a rmo:Revision. "
						+ " ?ref a rmo:Reference; rmo:references ?rev ."
						+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
						revisionGraph, identifier, identifier);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	
	
	/**
	 * Check whether the revision graph has a reference with the specified name
	 * 
	 * @param referenceName
	 *            the branch name to check
	 * @return true when branch already exists elsewhere false
	 */
	public boolean hasReference(final String referenceName) {
		String revisionGraph = this.getRevisionGraphUri();
		String queryASK = Config.prefixes
				+ String.format("ASK { GRAPH <%s> { ?ref a rmo:Reference; rdfs:label \"%s\".  }} ",
						revisionGraph, referenceName);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	
	/**
	 * Get the graph URI containing the full copy of a reference for a given
	 * reference name or revision number
	 * 
	 * @param referenceIdentifier
	 *            reference name or revision number
	 * @return first graph name of full graph for specified reference and graph
	 * @throws InternalErrorException 
	 */
	public String getReferenceGraph(final String referenceIdentifier) throws InternalErrorException {
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes + String.format("" 
				+ "SELECT ?graph " 
				+ "WHERE { GRAPH  <%s> {" 
				+ "	?ref a rmo:Reference; "
				+ "		rmo:references ?rev;" 
				+ "		rmo:fullGraph ?graph."
				+ " ?rev a rmo:Revision."
				+ "	{?ref rdfs:label \"%s\"} UNION {?rev rmo:revisionNumber \"%s\"}" 
				+ "} }", revisionGraph, referenceIdentifier, referenceIdentifier);
		this.logger.debug(query);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?graph").toString();
		} else {
			throw new InternalErrorException("No reference graph found for graph <"+graphName+"> and identifier \""+ referenceIdentifier+"\"");
		}
	}
	
	/**
	 * Get the MASTER revision number of a graph.
	 * 
	 * @param graphName
	 *            the graph name
	 * @return the MASTER revision number
	 */
	public String getMasterRevisionNumber() {
		logger.info("Get MASTER revision number of graph " + graphName);

		String revisionGraph = this.getRevisionGraphUri();
		String queryString = Config.prefixes + String.format(""
				+ "SELECT ?revisionNumber "  
				+ "WHERE { GRAPH <%s> {"
				+ "	?master a rmo:Master; rmo:references ?revision . "
				+ "	?revision rmo:revisionNumber ?revisionNumber . " 
				+ "} }", revisionGraph);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryString);
		if (results.hasNext()){
			QuerySolution qs = results.next();
			return qs.getLiteral("?revisionNumber").getString();
		}
		else {
			return null;
		}
	}
	
	/** Returns new unique revision number for this graph
	 * 
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public String getNextRevisionNumber() throws InternalErrorException {
		// create UID and check whether the uid number already in named graph exist, if yes , than create it once again,
		// if not , return this one
		
		//UID nextNumberUid = new UID();
		//String nextNumber = nextNumberUid.toString();
		int nextNumber;
		
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes
				+ String.format(
					"SELECT ?nr "
					+ "WHERE { GRAPH <%s> {"
					+ "	?rev a rmo:Revision; rmo:revisionNumber ?nr ."
					+ " } "
					+ "}ORDER BY DESC(xsd:integer(?nr))", revisionGraph);
		try {
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
			QuerySolution qs = results.next();
			nextNumber = qs.getLiteral("?nr").getInt()+1;
		}
		catch (Exception e){
			nextNumber = 0;
		}
		
		int count = 0;
		while (this.hasRevisionNumber(""+nextNumber)){
			nextNumber++;
			count++;
			if (count==100)
				throw new InternalErrorException("No new revision number found");
		}
		
		return ""+nextNumber;
	}
	
	/**
	 * Deletes all revision information for this revision graph including all full
	 * graphs and information in the R43ples system.
	 * 
	 */
	public void purgeRevisionInformation() {
		logger.info("Purge revision information of graph " + graphName);
		// Drop all full graphs as well as add and delete sets which are related
		// to specified graph
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes	+ String.format(""
				+ "SELECT DISTINCT ?graph "
				+ "WHERE { GRAPH <%s> {"
				+ " {?rev rmo:addSet ?graph}" 
				+ " UNION {?rev rmo:deleteSet ?graph}"
				+ " UNION {?ref rmo:fullGraph ?graph}"
				+ "} }", revisionGraph);
				
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			if (qs.get("?graph").isResource()) {
					String graph = qs.getResource("graph").toString();
					TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + graph + ">");
					logger.debug("Graph deleted: " + graph);
			}
		}
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", revisionGraph));
		
		// Remove information from revision graph
		String queryDelete = Config.prefixes + String.format(
					   	"DELETE { "
						+ "GRAPH <%s> {	<%s> ?p ?o.}"
						+ "}" 
						+ "WHERE {"
						+ "	GRAPH <%s> { <%s> a rmo:Graph; ?p ?o.}" 
						+ "}"
						, Config.revision_graph, graphName, Config.revision_graph, graphName);
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryDelete);
	}
	
	/**
	 * get the names of all branches of the revision graph
	 * @return list of the name of branch
	 * */
	public ArrayList<String> getAllBranchNames() {
		logger.info("Get all branch names of graph "+ graphName);
		ArrayList<String> list = new ArrayList<String>();	
		if (graphName != null) {
			String revisionGraph = this.getRevisionGraphUri();
			String sparqlQuery = Config.prefixes
					+ String.format(
					  "SELECT DISTINCT ?label %n"
					+ "FROM <%s> %n"
					+ "WHERE { %n"
					+ "	?branch a rmo:Branch ;"
					+ "		rdfs:label ?label . "
					+ "} %n"
					+ "ORDER BY ?label", revisionGraph);
			
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				list.add(qs.getLiteral("label").toString());
			}		
		}
		logger.debug("All branches: " +list);
		return list;
	}
	
	
	/**
	 * Get the reference URI of a branch for a given reference name or revision number
	 * 
	 * @param referenceIdentifier
	 *            reference name or revision number
	 * @return URI of identified revision
	 * @throws InternalErrorException 
	 */
	public String getBranchUri(final String referenceIdentifier) throws InternalErrorException {
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes
				+ String.format("SELECT ?ref " 
						+ "WHERE { GRAPH <%s> {"
						+ "	?ref a rmo:Branch; rmo:references ?rev."
						+ " ?rev a rmo:Revision."
						+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref rdfs:label \"%s\" .}"
						+ "} }",
						revisionGraph, referenceIdentifier, referenceIdentifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalErrorException("Identifier is not unique for specified graph name: "
						+ referenceIdentifier);
			}
			return qs.getResource("?ref").toString();
		} else {
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ referenceIdentifier);
		}
	}
	
	/**
	 * Get the revision URI for a given reference name or revision number
	 * 
	 * @param revisionIdentifier
	 *            reference name or revision number
	 * @return URI of identified revision
	 * @throws InternalErrorException 
	 */
	public String getRevisionUri(final String revisionIdentifier) throws InternalErrorException {
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes
				+ String.format(
						"SELECT ?rev WHERE { GRAPH <%s> {"
							+ "{?rev a rmo:Revision; rmo:revisionNumber \"%s\" .}"
							+ "UNION {?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\" .}"
							+ "} }", revisionGraph, revisionIdentifier, revisionIdentifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				logger.error("Identifier not unique: " + revisionIdentifier);
				throw new InternalErrorException("Identifier not unique: " + revisionIdentifier);
			}
			return qs.getResource("?rev").toString();
		} else {
			logger.error("No Revision or Reference found with identifier: " + revisionIdentifier);
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ revisionIdentifier);
		}
	}



	/**
	 * Get the revision number of a given reference name.
	 * 
	 * @param referenceName
	 *            the reference name
	 * @return the revision number of given reference name
	 * @throws InternalErrorException 
	 */
	public String getRevisionNumber(final String referenceName) throws InternalErrorException {
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes + String.format(""
				+ "SELECT ?revNumber WHERE { GRAPH <%s> {"
				+ "	?rev a rmo:Revision; rmo:revisionNumber ?revNumber."
				+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\".}"
				+ "} }", 
				revisionGraph, referenceName, referenceName);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalErrorException("Identifier not unique: " + referenceName);
			}
			return qs.getLiteral("?revNumber").toString();
		} else {
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ referenceName);
		}
	}

		
	/**
	 * checks whether the revision number already exist
	 * @param revisionNumber
	 * @return boolean*/
	
	public boolean hasRevisionNumber(final String revisionNumber) {
		String revisionGraph = this.getRevisionGraphUri();
		String queryASK = Config.prefixes
				+ String.format(""
						+ "ASK {"
						+ "	GRAPH <%1$s> { " 
						+ " 	{ ?rev a rmo:Revision; rmo:revisionNumber \"%2$s\". }"
						+ "		UNION "
						+ "		{?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%2$s\" .}"
						+ "} } ",
						revisionGraph, revisionNumber);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	
	/**
	 * Get the URI of the full graph of the specified branch if it exists. Otherwise return null

	 * @param branchURI
	 * @return URI of the full graph
	 */
	public String getFullGraphUri(final String branchURI) {
		String revisionGraph = this.getRevisionGraphUri();
		String query = String.format(
				  "SELECT ?fullGraphURI %n"
			    + "WHERE { GRAPH <%s> {%n"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#fullGraph> ?fullGraphURI . %n"
				+ "} }", revisionGraph, branchURI);
			
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?fullGraphURI").toString();
		} else {
			return null;
		}
	}
	
	
	/** Get SDD for this named graph
	 * 
	 * @param sdd
	 * @return specified SDD if not null otherwise the default SDD for the specified graph
	 * @throws InternalErrorException
	 */
	public String getSDD(String sdd)
			throws InternalErrorException {
		if (sdd != null && !sdd.equals("")) {
			return sdd;
		} else {
			// Default SDD
			// Query the referenced SDD
			String querySDD = String.format(
					  "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
					+ "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> %n"
					+ "SELECT ?defaultSDD %n"
					+ "WHERE { GRAPH <%s> {	%n"
					+ "	<%s> a rmo:Graph ;%n"
					+ "		sddo:hasDefaultSDD ?defaultSDD . %n"
					+ "} }", Config.revision_graph, this.graphName);
			
			ResultSet resultSetSDD = TripleStoreInterfaceSingleton.get().executeSelectQuery(querySDD);
			if (resultSetSDD.hasNext()) {
				QuerySolution qs = resultSetSDD.next();
				return qs.getResource("?defaultSDD").toString();
			} else {
				throw new InternalErrorException("Error in revision graph! Selected graph <" + this.graphName + "> has no default SDD referenced.");
			}
		}
	}

}
