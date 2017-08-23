package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new reference creation commit.
 *
 * @author Stephan Hensel
 */
public class ReferenceCreationCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ReferenceCreationCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

    /** The graph name. **/
    private String graphName;
    /** The reference name. **/
    private String referenceName;
    /** The revision identifier. **/
    private String revisionIdentifier;
    /** States if the created reference is a branch or a tag. (branch => true; tag => false) **/
    private boolean isBranch;

    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     */
    public ReferenceCreationCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        this.extractRequestInformation();
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates an reference creation commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param referenceName the reference name
     * @param revisionIdentifier the revision identifier (the corresponding revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @param isBranch states if the created reference is a branch or a tag. (branch => true; tag => false)
     * @throws InternalErrorException
     */
    protected ReferenceCreationCommitDraft(String graphName, String referenceName, String revisionIdentifier, String user, String message, boolean isBranch) throws InternalErrorException {
        super(null);
        this.setUser(user);
        this.setMessage(message);
        this.graphName = graphName;
        this.referenceName = referenceName;
        this.revisionIdentifier = revisionIdentifier;
        this.isBranch = isBranch;
        this.isCreatedWithRequest = false;
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        final Pattern patternBranchOrTagQuery = Pattern.compile("(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
                patternModifier);
        Matcher m = patternBranchOrTagQuery.matcher(getRequest().query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;
            String action = m.group("action");
            this.graphName = m.group("graph");
            this.revisionIdentifier = m.group("revision").toLowerCase();
            this.referenceName = m.group("name").toLowerCase();
            if (action.equals("TAG")) {
                this.isBranch = false;
            } else if (action.equals("BRANCH")) {
                this.isBranch = true;
            } else {
                throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
            }
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }
    }

    /**
     * Creates the commit draft as a new commit in the triple store and creates the corresponding revisions.
     *
     * @return the list of created commits
     */
    protected ReferenceCommit createCommitInTripleStore() throws InternalErrorException {
        RevisionGraph revisionGraph = new RevisionGraph(graphName);
        String referenceURI;
        String commitURI;
        if (isBranch) {
            referenceURI = getRevisionManagement().getNewBranchURI(revisionGraph, referenceName);
            commitURI = getRevisionManagement().getNewBranchCommitURI(revisionGraph, referenceName);
        } else {
            referenceURI = getRevisionManagement().getNewTagURI(revisionGraph, referenceName);
            commitURI = getRevisionManagement().getNewTagCommitURI(revisionGraph, referenceName);
        }

        addMetaInformation(referenceURI, commitURI);

        Revision usedRevision = new Revision(revisionGraph, revisionIdentifier, true);
        Reference generatedReference;
        if (isBranch) {
            generatedReference = new Branch(revisionGraph, referenceName, referenceURI);
        } else {
            generatedReference = new Tag(revisionGraph, referenceName, referenceURI);
        }

        return new ReferenceCommit(revisionGraph, commitURI, getUser(), getTimeStamp(), getMessage(), usedRevision, generatedReference);
    }

    /**
     * Creates a new full graph for the new reference and and adds the necessary meta data.
     *
     * @param referenceURI the reference URI
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    private void addMetaInformation(String referenceURI, String commitURI) throws InternalErrorException {
        if (isBranch) {
            logger.info("Create new branch '" + referenceName + "' for graph " + graphName);
        } else {
            logger.info("Create new tag '" + referenceName + "' for graph " + graphName);
        }

        RevisionGraph graph = new RevisionGraph(graphName);
        String revisionGraph = graph.getRevisionGraphUri();
        // Check branch existence
        if (graph.hasReference(referenceName)) {
            // Reference name is already in use
            logger.error("The reference name '" + referenceName + "' is for the graph '" + graphName
                    + "' already in use.");
            throw new IdentifierAlreadyExistsException("The reference name '" + referenceName
                    + "' is for the graph '" + graphName + "' already in use.");
        } else {
            // General variables
            String referenceTypeUri = isBranch ? "rmo:Branch" : "rmo:Tag";
            String revisionUri = graph.getRevisionUri(revisionIdentifier);
            String personUri = RevisionManagementOriginal.getUserName(getUser());

            // Create a new commit (activity)
            String queryContent = String.format(""
                            + "<%s> a %sCommit, rmo:Commit; "
                            + "	prov:wasAssociatedWith <%s> ;"
                            + "	prov:generated <%s> ;"
                            + " prov:used <%s> ;"
                            + "	dc-terms:title \"%s\" ;"
                            + "	prov:atTime \"%s\" .%n",
                    commitURI, referenceTypeUri, personUri, referenceURI, revisionUri, getMessage(), getTimeStamp());

            // Create new reference (branch/tag)
            queryContent += String.format(""
                            + "<%s> a %s, rmo:Reference; "
                            + " rmo:fullGraph <%s>; "
                            + "	prov:wasDerivedFrom <%s>; "
                            + "	rmo:references <%s>; "
                            + "	rdfs:label \"%s\". ",
                    referenceURI, referenceTypeUri, referenceURI, revisionUri, revisionUri, referenceName);

            // Update full graph of branch
            RevisionManagementOriginal.generateFullGraphOfRevision(graphName, revisionIdentifier, referenceURI);

            // Execute queries
            String query = Config.prefixes
                    + String.format("INSERT DATA { GRAPH <%s> { %s } } ;", revisionGraph, queryContent);
            getTripleStoreInterface().executeUpdateQuery(query);
        }
    }

}
