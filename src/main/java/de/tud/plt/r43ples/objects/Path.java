package de.tud.plt.r43ples.objects;

import de.tud.plt.r43ples.management.RevisionGraph;
import org.apache.log4j.Logger;

import java.util.LinkedList;

/**
 * This class provides the path between two revisions (from start revision to target revision).
 *
 * @author Stephan Hensel
 */
public class Path {

    /** The logger. **/
    private Logger logger = Logger.getLogger(Path.class);

    /** The start revision. */
    private Revision startRevision;
    /** The target revision. */
    private Revision targetRevision;
    /** The path from start to target revision. */
    private LinkedList<Revision> revisionPath;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param startRevision the start revision of the path
     * @param targetRevision the target revision of the path
     */
    public Path(RevisionGraph revisionGraph, Revision startRevision, Revision targetRevision) {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();

        this.startRevision = startRevision;
        this.targetRevision = targetRevision;

        revisionPath = new LinkedList<>();
    }

    /**
     * Adds a revision to the revision path.
     *
     * @param revision the revision to add to the end of the list
     */
    public void addRevision(Revision revision) {
        revisionPath.add(revision);
    }

    /**
     * Get the revision path. The revision path starts with the start revision and ends with the target revision.
     *
     * @return the revision path
     */
    public LinkedList<Revision> getRevisionPath() {
        return revisionPath;
    }

}