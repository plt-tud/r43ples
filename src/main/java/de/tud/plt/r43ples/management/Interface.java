package de.tud.plt.r43ples.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class Interface {

	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Interface.class);

	private static final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	/**
	 * 
//	 * @param query
//	 *            R43ples query string
//	 * @param format
//	 *            serialization format of the result
	 * @param query_rewriting
	 *            option if query rewriting should be enabled
	 * @return string containing result of the query
	 * @throws InternalErrorException
	 */
	public static String sparqlSelectConstructAsk(final R43plesRequest request,
			final boolean query_rewriting) throws InternalErrorException {
		String result;
		if (query_rewriting) {
			String query_rewritten = SparqlRewriter.rewriteQuery(request.query_sparql);
			result = TripleStoreInterfaceSingleton.get()
					.executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + query_rewritten, request.format);
		} else {
			result = getSelectConstructAskResponseClassic(request.query_sparql, request.format);
		}
		return result;
	}

	/**
	 * @param query
	 * @param format
	 * @return
	 * @throws InternalErrorException
	 */
	private static String getSelectConstructAskResponseClassic(final String query, final String format)
			throws InternalErrorException {
		final Pattern patternSelectFromPart = Pattern.compile(
				"(?<type>FROM|GRAPH)\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")",
				Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String type = m.group("type");
			String revisionNumber = m.group("revision").toLowerCase();
			String newGraphName;

			RevisionGraph graph = new RevisionGraph(graphName);
					
			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER
				// revisions are already created in the named graphs
				newGraphName = graphName;
			} else {
				if (graph.hasBranch(revisionNumber)) {
					newGraphName = graph.getReferenceGraph(revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision
					// must be generated - saved in graph <graphName-revisionNumber>
					newGraphName = graphName + "-" + revisionNumber;
					RevisionManagementOriginal.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);

		}
		String response = TripleStoreInterfaceSingleton.get()
				.executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + queryM, format);
		return response;
	}



	public static void sparqlDropGraph(final String query) throws QueryErrorException {
		final Pattern patternDropGraph = Pattern.compile("DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
				patternModifier);
		Matcher m = patternDropGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			String graphName = m.group("graph");
			RevisionGraph graph = new RevisionGraph(graphName);
			graph.purgeRevisionInformation();
		}
		if (!found) {
			throw new QueryErrorException("Query contain errors:\n" + query);
		}
	}




}
