package de.tud.plt.r43ples.management;

import de.tud.plt.r43ples.exception.InternalErrorException;

/** Collection of information for creating a new revision
 * This class does not modify any data in the triplestore.
 * 
 * @author Markus Graube
 *
 */
public class RevisionDraft {

	public String graphName;
	public String revisionName;
	public String revisionNumber;
	public String revisionNumber2 = null;
	public String revisionGraph;
	
	
	public String newRevisionNumber;
	public String addSetURI;
	public String deleteSetURI;
	public String referenceFullGraph;
	
	private RevisionGraph graph;
	
	
	
	public RevisionDraft(final String graphName, final String revisionName) throws InternalErrorException {
		this.graphName = graphName;
		this.graph = new RevisionGraph(graphName);
		this.revisionGraph = graph.getRevisionGraphUri();
		this.revisionName = revisionName;
		this.revisionNumber = graph.getRevisionNumber(revisionName);
		
		this.newRevisionNumber = graph.getNextRevisionNumber();
		
		this.addSetURI = graphName + "-addSet-" + newRevisionNumber;
		this.deleteSetURI = graphName + "-deleteSet-" + newRevisionNumber;
		this.referenceFullGraph = graph.getReferenceGraph(revisionName);
	}
	
	
	
	public boolean equals(final String graphName, final String revisionNumber) throws InternalErrorException{
		RevisionGraph otherGraph = new RevisionGraph(graphName);
		String otherRevisionNumber = otherGraph.getRevisionNumber(revisionNumber);
		return ((this.graphName.equals(graphName)) && (this.revisionNumber.equals(otherRevisionNumber)));
	}
		
	
}
