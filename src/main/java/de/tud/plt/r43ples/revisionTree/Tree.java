package de.tud.plt.r43ples.revisionTree;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

/**
 * This class provides a tree structure to store revision structure.
 * Uses the {@link Revision revision} class.
 * 
 * @author Stephan Hensel
 *
 */
public class Tree {
	
	/** The logger. **/
	private static Logger logger = Logger.getLogger(Tree.class);

	/** The map to store tree data */
	private HashMap<String, Revision> map = new HashMap<String, Revision>();
	
	
	
	/**
	 * Creates a tree with all revisions (with predecessors and successors and
	 * references of tags and branches)
	 * 
	 * @param revisionGraph
	 *            the graph name
	 */
	public Tree (final String revisionGraph) {
		logger.debug("Start creation of revision tree of graph " + revisionGraph + "!");

		// create query
		String queryRevisions = RevisionManagement.prefixes + String.format(""
						+ "SELECT ?uri ?revNumber ?fullGraph " 
						+ "WHERE {"
						+ "GRAPH <%s> {"
						+ "	?uri a rmo:Revision;" 
						+ "		rmo:revisionNumber ?revNumber."
						+ "	OPTIONAL { ?branch rmo:references ?uri; rmo:fullGraph ?fullGraph.}" 
						+ "} }",
						revisionGraph);
		ResultSet resultsCommits = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryRevisions);
		while (resultsCommits.hasNext()) {
			QuerySolution qsCommits = resultsCommits.next();
			String revision = qsCommits.getResource("?uri").toString();
			String revisionNumber = qsCommits.getLiteral("?revNumber").getString();
			String fullGraph = "";
			if (qsCommits.getResource("?fullGraph") != null)
				fullGraph = qsCommits.getResource("?fullGraph").toString();

			logger.debug("Found revision: " + revision + ".");
			this.addNode(revisionNumber, revision, fullGraph);
		}

		String queryRevisionConnection = RevisionManagement.prefixes + String.format(""
						+ "SELECT ?revNumber ?preRevNumber "  
						+ "WHERE { GRAPH <%s> {"
						+ " ?rev a rmo:Revision;" 
						+ "	rmo:revisionNumber ?revNumber; " 
						+ "	prov:wasDerivedFrom ?preRev. "
						+ "?preRev rmo:revisionNumber ?preRevNumber. " 
						+ " } }", revisionGraph);
		ResultSet resultRevConnection = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryRevisionConnection);
		while (resultRevConnection.hasNext()) {
			QuerySolution qsCommits = resultRevConnection.next();
			String revision = qsCommits.getLiteral("?revNumber").toString();
			String preRevision = qsCommits.getLiteral("?preRevNumber").toString();
			this.addEdge(revision, preRevision);
		}
	}
	
	/**
	 * Add new node to tree.
	 * 
	 * @param revisionNumber 
	 * 			revision number
	 * @param revisionUri 
	 * 			URI which specifies this revision
	 * @param fullGraph
	 * 			URI of the graph which holds a full copy if available
	 */
	private void addNode(String revisionNumber, String revisionUri, String fullGraph) {
		Revision node = new Revision(revisionNumber, revisionUri, fullGraph);
		map.put(revisionNumber, node);
	}
	
	/**
	 * Add edge between to nodes.
	 * 
	 * @param successor revision number of the successor node
	 * @param predecessor revision number of the predecessor node
	 */
	private void addEdge(String successor, String predecessor) {
		Revision nodeS = map.get(successor);
		Revision nodeP = map.get(predecessor);
		nodeP.addSuccessor(nodeS);
	}	
	
	/**
	 * Calculate the path from specified revision to a referenced revision.
	 * 
	 * @param revisionNumber the revision number to calculate the path for
	 * @return linked list with all revisions from specified revision number to referenced revision 
	 */
	public LinkedList<Revision> getPathToRevision(String revisionNumber) {
		// TODO: make path resolving more clever; not always using first successor but breadth-first search
		LinkedList<Revision> list = new LinkedList<Revision>();
		Revision node = map.get(revisionNumber);
		
		// Add all revision numbers from specified revision to first found revision with a full graph
		list.addFirst(node);
		while (node.getFullGraph().equals("")) {
			node = node.getFirstSuccessor();
			list.addFirst(node);
		}
		return list;
	}	
	
}
