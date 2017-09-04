/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import org.apache.log4j.Logger;

import java.util.LinkedList;

public class ChangeSetPath {

    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(ChangeSetPath.class);

    /**
     * The start revision.
     */
    private Revision startRevision;
    /**
     * The target revision.
     */
    private Revision targetRevision;
    /**
     * The path from start to target revision.
     */
    private LinkedList<ChangeSet> changeSets;

    /**
     * The corresponding revision graph.
     */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph  the revision graph
     * @param startRevision  the start revision of the path
     * @param targetRevision the target revision of the path
     */
    public ChangeSetPath(RevisionGraph revisionGraph, Revision startRevision, Revision targetRevision) {
        this.revisionGraph = revisionGraph;

        this.startRevision = startRevision;
        this.targetRevision = targetRevision;

        changeSets = new LinkedList<>();
    }

    /**
     * Adds a changeset to the revision path end.
     *
     * @param changeSet the revision to add to the end of the list
     */
    public void addRevisionToPathEnd(ChangeSet changeSet) {
        changeSets.add(changeSet);
    }

    /**
     * Adds a changeSet to the changeSet path start.
     *
     * @param changeSet the changeSet to add to the end of the list
     */
    public void addRevisionToPathStart(ChangeSet changeSet) {
        changeSets.addFirst(changeSet);
    }

    /**
     * Get the revision path. The revision path starts with the start revision and ends with the target revision.
     *
     * @return the revision path
     */
    public LinkedList<ChangeSet> getRevisionPath() {
        return changeSets;
    }

    public Revision getTargetRevision() {
        return targetRevision;
    }

    public Revision getStartRevision() {
        return startRevision;
    }
}
