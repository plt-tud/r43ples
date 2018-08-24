package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.Tag;
import de.tud.plt.r43ples.existentobjects.TagCommit;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.management.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

/**
 * Collection of information for creating a new tag commit.
 *
 * @author Stephan Hensel
 */
public class TagCommitDraft extends ReferenceCommitDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(TagCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;


    /**
     * The constructor.
     * Creates a tag commit draft by using the corresponding meta information.
     *
     * @param revisionGraph the revision graph
     * @param referenceIdentifier the reference identifier
     * @param baseRevision the revision identifier (the corresponding revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @throws InternalErrorException
     */
    protected TagCommitDraft(RevisionGraph revisionGraph, String referenceIdentifier, Revision baseRevision, String user, String message) throws InternalErrorException {
        super(revisionGraph, referenceIdentifier, baseRevision, user, message, true);
    }

    /**
     * Creates the commit draft as a new commit in the triple store.
     *
     * @return the created tag commit
     */
    protected TagCommit createInTripleStore() throws InternalErrorException {
        String commitURI = getUriCalculator().getNewTagCommitURI(getRevisionGraph(), getReferenceIdentifier());

        // Check tag existence
        if (getRevisionGraph().hasReference(getReferenceIdentifier())) {
            // Reference name is already in use
            logger.error("The reference name '" + getReferenceIdentifier() + "' is for the graph '" + getRevisionGraph().getGraphName()
                    + "' already in use.");
            throw new IdentifierAlreadyExistsException("The reference name '" + getReferenceIdentifier()
                    + "' is for the graph '" + getRevisionGraph().getGraphName() + "' already in use.");
        }

        TagDraft tagDraft = new TagDraft(getUriCalculator(), getRevisionGraph(), getBaseRevision(), getReferenceIdentifier());
        Tag generatedTag = tagDraft.createInTripleStore();

        addMetaInformation(generatedTag.getReferenceURI(), commitURI);



        return new TagCommit(getRevisionGraph(), commitURI, getUser(), getTimeStamp(), getMessage(), getBaseRevision(), generatedTag);
    }

    /**
     * Adds the necessary meta data.
     *
     * @param referenceURI the reference URI
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    private void addMetaInformation(String referenceURI, String commitURI) {
        logger.info("Create new tag '" + getReferenceIdentifier() + "' for graph " + getRevisionGraph().getGraphName());

        // General variables
        String personUri = Helper.getUserURI(getUser());

        // Create a new commit (activity)
        String queryContent = String.format(""
                        + "<%s> a rmo:TagCommit, rmo:ReferenceCommit, rmo:Commit ; "
                        + "	rmo:wasAssociatedWith <%s> ;"
                        + "	rmo:generated <%s> ;"
                        + " rmo:used <%s> ;"
                        + "	rmo:commitMessage \"%s\" ;"
                        + "	rmo:timeStamp \"%s\" .%n",
                commitURI, personUri, referenceURI, getBaseRevision().getRevisionURI(), getMessage(), getTimeStamp());

        // Execute queries
        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } } ;", getRevisionGraph().getRevisionGraphUri(), queryContent);
        getTripleStoreInterface().executeUpdateQuery(query);

    }

}
