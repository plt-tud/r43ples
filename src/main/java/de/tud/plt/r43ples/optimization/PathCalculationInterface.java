package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Path;
import de.tud.plt.r43ples.existentobjects.Revision;


/**
 * This interface provides methods to calculate paths between revisions.
 *
 * @author Stephan Hensel
 *
 */
public interface PathCalculationInterface {


    /**
     * Get the path to the nearest revision which has a full graph.
     *
     * @param revision      revision where the search should start
     * @return path containing all revisions from start revision to next revision with a full graph
     */
    Path getPathToRevisionWithFullGraph(Revision revision) throws InternalErrorException;


    /**
     * Get the common revision of the specified revisions which has the shortest path to the two.
     * To ensure wise results the revisions should be terminal branch nodes.
     *
     * @param revision1     the first revision should be a terminal branch node
     * @param revision2     the second revision should be a terminal branch node
     * @return the nearest common revision
     * @throws InternalErrorException
     */
    Revision getCommonRevisionWithShortestPath(Revision revision1, Revision revision2) throws InternalErrorException;

    /**
     * Calculate the path from start revision to target revision.
     * Example: target rmo:wasDerivedFrom source
     *
     * @param startRevision  the start revision
     * @param targetRevision the target revision
     * @return path containing all revisions from start revision to target revision
     * @throws InternalErrorException
     */
    Path getPathBetweenStartAndTargetRevision(Revision startRevision, Revision targetRevision) throws InternalErrorException;


    ChangeSetPath getChangeSetsBetweenStartAndTargetRevision(Revision startRevision, Revision targetRevision) throws InternalErrorException;

    ChangeSetPath getPathOfChangeSets(Revision revision) throws InternalErrorException;

}