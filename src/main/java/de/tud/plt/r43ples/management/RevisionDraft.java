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
	public String revisionGraph;
	
	
	public String newRevisionNumber;
	public String addSetURI;
	public String deleteSetURI;
	public String referenceFullGraph;
	
	
	
	public RevisionDraft(final String graphName, final String revisionName) throws InternalErrorException {
		this.graphName = graphName;
		this.revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		this.revisionName = revisionName;
		this.revisionNumber= RevisionManagement.getRevisionNumber(revisionGraph, revisionName);
		
		this.newRevisionNumber = RevisionManagement.getNextRevisionNumber(graphName);
		
		this.addSetURI = graphName + "-addSet-" + newRevisionNumber;
		this.deleteSetURI = graphName + "-deleteSet-" + newRevisionNumber;
		this.referenceFullGraph = RevisionManagement.getReferenceGraph(graphName, revisionName);
	}
	
	
	
	public boolean equals(final String graphName, final String revisionNumber) throws InternalErrorException{
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		String revNumber = RevisionManagement.getRevisionNumber(revisionGraph, revisionNumber);
		return ((this.graphName.equals(graphName)) && (this.revisionNumber.equals(revNumber)));
	}
	
	
}
