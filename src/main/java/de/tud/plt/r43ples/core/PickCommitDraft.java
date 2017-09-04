package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.optimization.PathCalculationInterface;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new pick commit.
 *
 * @author Stephan Hensel
 */
public class PickCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(PickCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternPickQuery = Pattern.compile(
            "PICK\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*REVISION\\s*\"(?<startRevisionIdentifier>[^\"]*?)\"\\s*(TO\\s*REVISION\\s*\"(?<endRevisionIdentifier>[^\"]*?)\"\\s*)?INTO\\s*BRANCH\\s*\"(?<targetBranchIdentifier>[^\"]*?)\"",
            patternModifier);

    /** The start revision identifier. **/
    private String startRevisionIdentifier;
    /** The end revision identifier. **/
    private String endRevisionIdentifier;
    /** The target branch identifier (into). **/
    private String targetBranchIdentifier;
    /** The graph name **/
    private String graphName;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;

    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;

    //Dependencies
    /** The path calculation interface to use. **/
    private PathCalculationInterface pathCalculationInterface;


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public PickCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        // Dependencies
        this.pathCalculationInterface = PathCalculationSingleton.getInstance();

        this.extractRequestInformation();
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates a pick commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param startRevisionIdentifier the start revision identifier
     * @param endRevisionIdentifier the end revision identifier
     * @param targetBranchIdentifier the target branch identifier
     * @param user the user
     * @param message the message
     * @throws InternalErrorException
     */
    protected PickCommitDraft(String graphName, String startRevisionIdentifier, String endRevisionIdentifier, String targetBranchIdentifier, String user, String message) throws InternalErrorException {
        super(null);
        // Dependencies
        this.pathCalculationInterface = PathCalculationSingleton.getInstance();

        this.setUser(user);
        this.setMessage(message);

        this.graphName = graphName;
        this.revisionGraph = new RevisionGraph(graphName);
        this.startRevisionIdentifier = startRevisionIdentifier;
        this.endRevisionIdentifier = endRevisionIdentifier;
        this.targetBranchIdentifier = targetBranchIdentifier;

        this.isCreatedWithRequest = false;
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        Matcher m = patternPickQuery.matcher(getRequest().query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;

            graphName = m.group("graph");
            revisionGraph = new RevisionGraph(graphName);

            startRevisionIdentifier = m.group("startRevisionIdentifier");
            endRevisionIdentifier = m.group("endRevisionIdentifier");
            targetBranchIdentifier = m.group("targetBranchIdentifier");

            logger.debug("graph: " + graphName);
            logger.debug("startRevisionIdentifier: " + startRevisionIdentifier);
            logger.debug("endRevisionIdentifier: " + endRevisionIdentifier);
            logger.debug("targetBranchIdentifier: " + targetBranchIdentifier);
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }
    }

    /**
     * Tries to create a new commit draft as a new commit in the triple store.
     * If possible it will create the corresponding revision and the meta data.
     *
     * @return the commit (has attribute which indicates if the commit was executed or not)
     * @throws InternalErrorException
     */
    protected PickCommit createCommitInTripleStore() throws InternalErrorException {

        if (!getRevisionManagement().checkNamedGraphExistence(graphName)) {
            logger.warn("Graph <" + graphName + "> does not exist.");
            throw new InternalErrorException("Graph <" + graphName + "> does not exist.");
        }

        // Check if it is a valid target branch identifier
        if (!(revisionGraph.hasBranch(targetBranchIdentifier))) {
            throw new InternalErrorException("No terminal nodes were used");
        }

        ArrayList<Revision> usedSourceRevisions = new ArrayList<>();
        ArrayList<Revision> generatedRevisions = new ArrayList<>();

        Branch usedTargetBranch = revisionGraph.getBranch(targetBranchIdentifier, true);
        Revision usedTargetRevision = new Revision(revisionGraph, revisionGraph.getRevisionUri(targetBranchIdentifier), false);
        Path path = null;
        Revision startRevision = new Revision(revisionGraph, startRevisionIdentifier, true);
        Revision endRevision;
        if (endRevisionIdentifier != null) {
            endRevision = new Revision(revisionGraph, endRevisionIdentifier, true);
            path = pathCalculationInterface.getPathBetweenStartAndTargetRevision(revisionGraph, endRevision, startRevision);
        }

        String commitURI = getRevisionManagement().getNewPickCommitURI(revisionGraph, startRevisionIdentifier, endRevisionIdentifier, targetBranchIdentifier, usedTargetRevision.getRevisionIdentifier());

        // Copy revisions
        Revision generatedRevision = null;
        if ((path == null) || (path.getRevisionPath().size() == 1)) {
            generatedRevision = copyRevisionToTargetBranch(startRevision, usedTargetRevision, usedTargetBranch, commitURI);
            usedSourceRevisions.add(startRevision);
            generatedRevisions.add(generatedRevision);
        } else {
            Iterator<Revision> iteRev = path.getRevisionPath().iterator();
            while(iteRev.hasNext()) {
                Revision currentRevision = iteRev.next();
                generatedRevision = copyRevisionToTargetBranch(currentRevision, usedTargetRevision, usedTargetBranch, commitURI);
                usedSourceRevisions.add(currentRevision);
                generatedRevisions.add(generatedRevision);
            }
        }

        return addMetaInformation(generatedRevision, usedTargetRevision, usedTargetBranch, commitURI, usedSourceRevisions, generatedRevisions);
    }

    /**
     * Adds meta information for the commit to the revision graph.
     *
     * <img src="{@docRoot}../../doc/revision management description/r43ples-pick.png" />
     *
     * @param generatedRevision the last generated revision
     * @param usedTargetRevision the used target revision (into)
     * @param usedTargetBranch the used target branch (from)
     * @param commitURI the commit URI
     * @param usedSourceRevisions the used revisions
     * @param generatedRevisions the generated revisions
     * @return the created commit
     * @throws InternalErrorException
     */
    private PickCommit addMetaInformation(Revision generatedRevision, Revision usedTargetRevision, Branch usedTargetBranch, String commitURI, ArrayList<Revision> usedSourceRevisions, ArrayList<Revision> generatedRevisions) throws InternalErrorException {

        String personUri = RevisionManagementOriginal.getUserURI(getUser());

        // Create a new commit (activity)
        StringBuilder queryContent = new StringBuilder(1000);
        queryContent.append(String.format(
                "<%s> a rmo:PickCommit, rmo:Commit; "
                        + "	prov:wasAssociatedWith <%s> ;"
                        + "	dc-terms:title \"%s\" ;"
                        + "	prov:atTime \"%s\"^^xsd:dateTime ; %n"
                        + " rmo:usedTargetRevision <%s> ;"
                        + " rmo:usedTargetBranch <%s> ." +
                        "<%s> a rmo:Revision;" +
                        " rmo:revisionNumber \"%s\".",
                commitURI, personUri, getMessage(), getTimeStamp(),
                usedTargetRevision.getRevisionURI(), usedTargetBranch.getReferenceURI(),
                generatedRevision.getRevisionURI(), generatedRevision.getRevisionIdentifier()));

        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } }", revisionGraph.getRevisionGraphUri(),
                queryContent.toString());

        getTripleStoreInterface().executeUpdateQuery(query);

        // Move source branch to new revision
        moveBranchReference(revisionGraph.getRevisionGraphUri(), usedTargetBranch.getReferenceURI(), usedTargetRevision.getRevisionURI(), generatedRevision.getRevisionURI());

        // Update the target branch object
        usedTargetBranch = revisionGraph.getBranch(targetBranchIdentifier, true);

        return new PickCommit(revisionGraph, commitURI, getUser(), getTimeStamp(), getMessage(), usedSourceRevisions, usedTargetRevision, usedTargetBranch, generatedRevisions);
    }


    /**
     * Copies a revision and adds meta information to the revision graph.
     *
     * @param revisionToCopy the original revision to copy
     * @param targetBranch the target branch
     * @param commitURI the associated commit URI
     * @return the generated revision
     */
    private Revision copyRevisionToTargetBranch(Revision revisionToCopy, Revision derivedFromRevision, Branch targetBranch, String commitURI) throws InternalErrorException {

        String addSetContent = revisionToCopy.getAddSetContent();
        String deleteSetContent = revisionToCopy.getDeleteSetContent();

        RevisionDraft revisionDraft = new RevisionDraft(getRevisionManagement(), revisionGraph, derivedFromRevision.getRevisionIdentifier(), addSetContent, deleteSetContent);
        Revision generatedRevision = revisionDraft.createRevisionInTripleStore();

        // Create the corresponding meta data
        StringBuilder queryContentInsert = new StringBuilder(1000);
        queryContentInsert.append(String.format(
                "<%1$s> prov:wasDerivedFrom <%2$s> ;"
                        + "	prov:wasQuotedFrom <%3$s> ;"
                        + "	rmo:belongsTo <%4$s> ."
                + "<%5$s> prov:generated <%1$s> ; "
                        + " rmo:usedSourceRevision <%3$s> .",
                generatedRevision.getRevisionURI(), derivedFromRevision.getRevisionURI(), revisionToCopy.getRevisionURI(),
                targetBranch.getReferenceURI(), commitURI));

        String query = Config.prefixes	+ String.format(""
                        + "INSERT DATA { GRAPH <%1$s> { %2$s } }",
                revisionGraph.getRevisionGraphUri(), queryContentInsert.toString());
        getTripleStoreInterface().executeUpdateQuery(query);

        return generatedRevision;
    }

}