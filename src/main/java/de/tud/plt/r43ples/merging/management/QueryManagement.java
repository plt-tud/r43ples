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
	
	private final Pattern patternSelectAskConstructQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
	private final static Pattern patternSelectFromPart = Pattern.compile(
			"(?<type>FROM|GRAPH)\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternUpdateRevision = Pattern.compile(
			"(?<action>INSERT|DELETE|WHERE)(?<data>\\s*DATA){0,1}\\s*\\{\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	private final Pattern patternEmptyGraphPattern = Pattern.compile(
			"GRAPH\\s*<(?<graph>[^>]*)>\\s*\\{\\s*\\}",
			patternModifier);
	private final Pattern patternGraphWithRevision = Pattern.compile(
			"GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCreateGraph = Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternDropGraph = Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternBranchOrTagQuery = Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
			patternModifier);
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);
	private final Pattern patternMergeQuery =  Pattern.compile(
			"MERGE\\s*(?<action>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
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
