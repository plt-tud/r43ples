package de.tud.plt.r43ples.existentobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * The class RevisionGraph provides functionality for one named graph which is managed by R43ples
 * @author Markus Graube
 *
 */
public class RevisionGraph {

	/** The logger. **/
	private Logger logger = Logger.getLogger(Helper.class);

	/** The URI of the named graph which R43ples manages. **/
	private String graphName;
	/** The revision graph URI. **/
	private String revisionGraphURI;

	/**
	 * Constructs a RevisionGraph for the specified graphName
	 *
	 * @param graphName URI of the named graph which R43ples manages
	 */
	public RevisionGraph(final String graphName) {
		this.graphName = graphName;
		this.revisionGraphURI = null;
	}

	/**
	 * Constructs a RevisionGraph for the specified graphName
	 *
	 * @param graphName URI of the named graph which R43ples manages
	 * @param revisionGraphURI the revision graph URI
	 */
	public RevisionGraph(final String graphName, final String revisionGraphURI) {
		this.graphName = graphName;
		this.revisionGraphURI = revisionGraphURI;
	}

	/**
	 * Get the graph name.
	 *
	 * @return the graph name
	 */
	public String getGraphName() {
		return graphName;
	}

	/**
	 * Get the content of this revision graph by execution of CONSTRUCT.
	 *
	 * @param format RDF serialization format which should be returned
	 * @return the constructed graph content as specified RDF serialization format
	 */
	public String getContentOfRevisionGraph(final String format) {
		return Helper.getContentOfGraph(this.getRevisionGraphUri(), format);
	}


	/** returns the name of the named graph which stores all revision information for the specified revised named graph
	 * 
	 * @return uri of the revision graph for this graph
	 */
	public String getRevisionGraphUri() {
		if (revisionGraphURI == null) {
			String query = String.format(
					"SELECT ?revisionGraph "
							+ "WHERE { GRAPH <%s> {"
							+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#hasRevisionGraph> ?revisionGraph ."
							+ "} }", Config.revision_graph, graphName);

			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);

			if (results.hasNext()) {
				QuerySolution qs = results.next();
				revisionGraphURI = qs.getResource("?revisionGraph").toString();
			} else {
				return null;
			}
		}

		return revisionGraphURI;
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
						+ " { ?rev rmo:revisionIdentifier \"%s\"} UNION { ?ref rmo:referenceIdentifier \"%s\"} }} ",
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
				+ String.format("ASK WHERE { GRAPH <%s> { ?ref a rmo:Reference; rmo:referenceIdentifier \"%s\".  }} ",
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
				+ "		rmo:fullContent ?graph."
				+ " ?rev a rmo:Revision."
				+ "	{?ref rmo:referenceIdentifier \"%s\"} UNION {?rev rmo:revisionIdentifier \"%s\"}"
				+ "} }", revisionGraph, referenceIdentifier, referenceIdentifier);
		this.logger.debug(query);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?graph").toString();
		} else {
			throw new InternalErrorException("No reference graph found for graph <" + graphName + "> and identifier \"" + referenceIdentifier + "\"");
		}
	}
	
	/**
	 * Get the MASTER revision of this revision graph.
	 *
	 * @return the MASTER revision
	 * @throws InternalErrorException
	 */
	public Revision getMasterRevision() throws InternalErrorException{
        logger.info("Get MASTER revision of graph " + graphName);

		String revisionGraph = this.getRevisionGraphUri();
		String queryString = Config.prefixes + String.format(""
				+ "SELECT ?revisionIdentifier "
				+ "WHERE { GRAPH <%s> {"
				+ "	?master a rmo:Master; rmo:references ?revision . "
				+ " ?revision rmo:revisionIdentifier ?revisionIdentifier ."
				+ "} }", revisionGraph);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryString);
		if (results.hasNext()){
			QuerySolution qs = results.next();
			return this.getRevision(qs.getLiteral("?revisionIdentifier").toString());
		}
		else {
            throw new InternalErrorException("No master for graph <" + this.graphName + "> available");
        }
	}
	
	/**
	 * Returns new unique revision identifier for this graph.
	 *
	 * @return new revision identifier
	 * @throws InternalErrorException 
	 */
	public String getNextRevisionIdentifier() throws InternalErrorException {
		//
		int nextNumber;
		
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes
				+ String.format(
					"SELECT ?nr "
					+ "WHERE { GRAPH <%s> {"
					+ "	?rev a rmo:Revision; rmo:revisionIdentifier ?nr ."
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
				+ " UNION {?ref rmo:fullContent ?graph}"
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
					+ "		rmo:referenceIdentifier ?label . "
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
						+ "	{?rev rmo:revisionIdentifier \"%s\".} UNION {?ref rmo:referenceIdentifier \"%s\" .}"
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
	 * Get the revision object for a given reference name or revision number
	 *
	 * @param branchInformation the branch information (identifier or URI of the branch)
	 * @param isIdentifier identifies if the identifier or the URI of the branch is specified (identifier => true; URI => false)
	 * @return Branch object
	 * @throws InternalErrorException
	 */
	public Branch getBranch(final String branchInformation, final boolean isIdentifier) throws InternalErrorException {
		return new Branch(this, branchInformation, isIdentifier);
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
							+ "{?rev a rmo:Revision; rmo:revisionIdentifier \"%s\" .}"
							+ "UNION {?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rmo:referenceIdentifier \"%s\" .}"
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
	 * Get the revision object for a given reference name or revision number
	 *
	 * @param revisionIdentifier
	 *            reference name or revision number
	 * @return Revision object
	 * @throws InternalErrorException
	 */
	public Revision getRevision(final String revisionIdentifier) throws InternalErrorException {
		return new Revision(this, getRevisionUri(revisionIdentifier), false);
	}

    /**
     * Get the revision object for a given branch
     *
     * @param branch the referencing branch
     * @return Revision object
     * @throws InternalErrorException
     */
    public Revision getRevision(Branch branch) throws InternalErrorException {
        return new Revision(this, getRevisionUri(branch.getReferenceIdentifier()), false);
    }

	/**
	 * Get the revision identifier while it is not necessary to know if the specified identifier parameter is a reference or the resulting revision identifier itself.
	 *
	 * @param identifier the identifier to look for (referenc or revision identifier itself)
	 * @return the revision identifier
	 * @throws InternalErrorException 
	 */
	public String getRevisionIdentifier(final String identifier) throws InternalErrorException {
		String revisionGraph = this.getRevisionGraphUri();
		String query = Config.prefixes + String.format(""
				+ "SELECT ?revIdentifier WHERE { GRAPH <%s> {"
				+ "	?rev a rmo:Revision; rmo:revisionIdentifier ?revIdentifier."
				+ "	{?rev rmo:revisionIdentifier \"%s\".} UNION {?ref a rmo:Reference; rmo:references ?rev; rmo:referenceIdentifier \"%s\".}"
				+ "} }", 
				revisionGraph, identifier, identifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalErrorException("Identifier not unique: " + identifier);
			}
			return qs.getLiteral("?revIdentifier").toString();
		} else {
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ identifier);
		}
	}

		
	/**
	 * checks whether the revision number already exist
	 * @param revisionNumber
	 * @return boolean*/

	private boolean hasRevisionNumber(final String revisionNumber) {
		String revisionGraph = this.getRevisionGraphUri();
		String queryASK = Config.prefixes
				+ String.format(""
						+ "ASK {"
						+ "	GRAPH <%1$s> { " 
						+ " 	{ ?rev a rmo:Revision; rmo:revisionIdentifier \"%2$s\". }"
						+ "		UNION "
						+ "		{?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rmo:referenceIdentifier \"%2$s\" .}"
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
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#fullContent> ?fullGraphURI . %n"
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

	/**
	 * Get the commit by using the URI.
	 *
	 * @param commitURI the commit URI
	 * @return the commit
	 * @throws InternalErrorException
	 */
	public Commit getCommit(String commitURI) throws InternalErrorException {
		throw new InternalErrorException("Not implemented.");
		//TODO implement method
//        logger.info("Get corresponding commit of revision " + revisionIdentifier + ".");
//        String query = Config.prefixes + String.format(""
//                + "SELECT ?com "
//                + "WHERE { GRAPH  <%s> {"
//                + "	?com a rmo:Commit; "
//                + "	 prov:generated <%s>. "
//                + "} }", revisionGraphURI, revisionURI);
//        this.logger.debug(query);
//        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
//        if (resultSet.hasNext()) {
//            QuerySolution qs = resultSet.next();
//            return new Commit(revisionGraph, qs.getResource("?com").toString());
//        } else {
//            throw new InternalErrorException("No corresponding commit found for revision " + revisionIdentifier + ".");
//        }
	}

}
