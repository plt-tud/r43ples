package de.tud.plt.r43ples.merging.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Endpoint;

public class QueryManagement {
	private final static int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	private final static Pattern patternSelectFromPart = Pattern.compile(
			"(?<type>FROM|GRAPH)\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	
	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);
	

	/**
	 * @param query
	 * @param format
	 * @return
	 * @throws InternalErrorException 
	 */
	public static String getSelectConstructAskResponseClassic(final String query, final String format) throws InternalErrorException {
		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String type = m.group("type");
			String revisionNumber = m.group("revision").toLowerCase();
			String newGraphName;

			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER revisions are already created in the named graphs
				newGraphName = graphName;
			} else {
				if (RevisionManagement.isBranch(graphName, revisionNumber)) {
					newGraphName = RevisionManagement.getReferenceGraph(graphName, revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = graphName + "-temp";
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);
			
		}
		String result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(queryM, format);
		return result;
	}
	
	/**
	 * Produce the response for a SELECT, ASK or CONSTRUCT SPARQL query. can handle multiple
	 * graphs
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @return the response with HTTP header for every graph (revision number
	 *         and MASTER revision number)
	 * @throws InternalErrorException 
	 * @throws UnsupportedEncodingException 
	 */
	public static String getSelectConstructAskResponse(final String query, final String format) throws InternalErrorException {
		if (query.contains("OPTION r43ples:SPARQL_JOIN")) {
			
			logger.info("query 1 : "+ query);
			String query_rewritten = query.replace("OPTION r43ples:SPARQL_JOIN", "");
			logger.info("query 2 : "+ query_rewritten);

			query_rewritten = RewriteManagement.rewriteQuery(query_rewritten);
			
			logger.info("query 3 : "+ query_rewritten);

		//	String result = TripleStoreInterface.executeSelectConstructAskQuery(query_rewritten, format);
			
			String result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(query_rewritten, format);
			return result;
		}
		else {
			return QueryManagement.getSelectConstructAskResponseClassic(query, format);
		}
	}

}
