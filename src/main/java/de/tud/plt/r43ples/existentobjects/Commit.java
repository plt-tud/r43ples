package de.tud.plt.r43ples.existentobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent commit.
 *
 * @author Stephan Hensel
 */
public class Commit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(Commit.class);

    /** The commit URI. */
    private String commitURI;
    /** The commit message. */
    private String commitMessage;
    /** The commit time stamp. */
    private String commitTimeStamp;
    /** The associated person name. */
    private String commitAssociatedPersonName;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public Commit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.commitURI = commitURI;

        retrieveAdditionalInformation();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @param user the user
     * @param timeStamp the time stamp
     * @param message the message
     * @throws InternalErrorException
     */
    public Commit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.commitURI = commitURI;
        this.commitAssociatedPersonName = user;
        this.commitTimeStamp = timeStamp;
        this.commitMessage = message;
    }

    /**
     * Calculate additional information of the current commit and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current commit URI " + commitURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?title ?time ?person "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:Commit; "
                + "	 rmo:commitMessage ?title; "
                + "  rmo:atTime ?time; "
                + "  rmo:wasAssociatedWith ?person. "
                + "} }", revisionGraphURI, commitURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            commitMessage = qs.getLiteral("?title").toString();
            commitTimeStamp = qs.getLiteral("?timeStamp").toString();
            commitAssociatedPersonName = qs.getResource("?person").toString();
        } else {
            throw new InternalErrorException("No additional information found for commit URI " + commitURI + ".");
        }
    }

    /**
     * Get the commit URI.
     *
     * @return the commit URI
     */
    public String getCommitURI() {
        return commitURI;
    }

    /**
     * Get the commit message.
     *
     * @return the commit message
     */
    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * Get the commit time stamp.
     *
     * @return the commit time stamp
     */
    public String getCommitTimeStamp() {
        return commitTimeStamp;
    }

    /**
     * Get the associated person name.
     *
     * @return the associated person name
     */
    public String getCommitAssociatedPersonName() {
        return commitAssociatedPersonName;
    }

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

}
