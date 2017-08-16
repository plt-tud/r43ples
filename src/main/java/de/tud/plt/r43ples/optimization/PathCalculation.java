package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionGraph;
import de.tud.plt.r43ples.objects.Path;
import de.tud.plt.r43ples.objects.Revision;


/**
 * This interface provides methods to calculate paths between revisions.
 *
 * @author Stephan Hensel
 *
 */
public interface PathCalculation {


    /**
     * Get the path to the nearest revision which has a full graph.
     *
     * @param revisionGraph the revision graph
     * @param revision revision where the search should start
     * @return path containing all revisions from start revision to next revision with a full graph
     */
    Path getPathToRevisionWithFullGraph(RevisionGraph revisionGraph, Revision revision) throws InternalErrorException;



    /**
     * Get the common revision of the specified revisions which has the shortest path to the two.
     * To ensure wise results the revisions should be terminal branch nodes.
     *
     * @param revisionGraph the revision graph
     * @param revision1 the first revision should be a terminal branch node
     * @param revision2 the second revision should be a terminal branch node
     * @return the nearest common revision
     * @throws InternalErrorException
     */
    Revision getCommonRevisionWithShortestPath(RevisionGraph revisionGraph, Revision revision1, Revision revision2) throws InternalErrorException;

    /**
     * Calculate the path from start revision to target revision.
     *
     * @param revisionGraph the revision graph
     * @param startRevision the start revision
     * @param targetRevision the target revision
     * @return path containing all revisions from start revision to target revision
     * @throws InternalErrorException
     */
    Path getPathBetweenStartAndTargetRevision(RevisionGraph revisionGraph, Revision startRevision, Revision targetRevision) throws InternalErrorException;

}
