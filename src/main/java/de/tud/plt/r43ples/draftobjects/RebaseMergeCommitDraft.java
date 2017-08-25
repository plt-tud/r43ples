package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.optimization.PathCalculationInterface;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new rebase merge commit.
 *
 * @author Stephan Hensel
 */
public class RebaseMergeCommitDraft extends MergeCommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(FastForwardMergeCommitDraft.class);

    //Dependencies
    /** The path calculation interface to use. **/
    private PathCalculationInterface pathCalculationInterface;


    /**
     * The constructor.
     * Creates a fast forward merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param sdd the SDD URI to use
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    protected RebaseMergeCommitDraft(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String sdd, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        super(graphName, branchNameFrom, branchNameInto, user, message, sdd, MergeActions.MERGE, triples, type, with);
        // Dependencies
        this.pathCalculationInterface = PathCalculationSingleton.getInstance();
    }

    /**
     * Tries to create a new commit draft as a new commit in the triple store.
     * If possible it will create the corresponding revision and the meta data.
     *
     * @return the commit (has attribute which indicates if the commit was executed or not)
     */
    protected PickMergeCommit createCommitInTripleStore() throws InternalErrorException {
        String revisionGraphURI = getRevisionGraph().getRevisionGraphUri();
        String revisionUriFrom = getRevisionGraph().getRevisionUri(getBranchNameFrom());
        String revisionUriInto = getRevisionGraph().getRevisionUri(getBranchNameInto());

        if (!getRevisionManagement().checkNamedGraphExistence(getGraphName())) {
            logger.warn("Graph <" + getGraphName() + "> does not exist.");
            throw new InternalErrorException("Graph <" + getGraphName() + "> does not exist.");
        }

        // Check if from and into are different revisions
        if (revisionUriFrom.equals(revisionUriInto)) {
            // Branches are equal - throw error
            throw new InternalErrorException("Specified branches are equal");
        }

        // Check if both are terminal nodes
        if (!(getRevisionGraph().hasBranch(getBranchNameFrom()) && getRevisionGraph().hasBranch(getBranchNameInto()))) {
            throw new InternalErrorException("No terminal nodes were used");
        }

        Revision usedSourceRevision = new Revision(getRevisionGraph(), revisionUriFrom, false);
        Revision usedTargetRevision = new Revision(getRevisionGraph(), revisionUriInto, false);

        Branch usedSourceBranch = getRevisionGraph().getBranch(getBranchNameFrom(), true);
        Branch usedTargetBranch = getRevisionGraph().getBranch(getBranchNameInto(), true);

//        fullGraphCopy(usedSourceBranch.getReferenceURI(), usedTargetBranch.getReferenceURI());





        return addMetaInformation(usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch);
    }

    /**
     * Adds meta information for commit and revision to the revision graph.
     *
     * <img src="{@docRoot}../../doc/revision management description/r43ples-fastforward.png" />
     *
     * @param usedSourceRevision the used source revision (from)
     * @param usedSourceBranch the used source branch (from)
     * @param usedTargetRevision the used target revision (into)
     * @param usedTargetBranch the used target branch (from)
     * @return the created commit
     * @throws InternalErrorException
     */
    private PickMergeCommit addMetaInformation(Revision usedSourceRevision, Branch usedSourceBranch, Revision usedTargetRevision, Branch usedTargetBranch) throws InternalErrorException {

        String commitURI = getRevisionManagement().getNewRebaseMergeCommitURI(getRevisionGraph(), usedSourceRevision.getRevisionIdentifier(), usedTargetRevision.getRevisionIdentifier());
        String personUri = RevisionManagementOriginal.getUserURI(getUser());

        // Create a new commit (activity)
        StringBuilder queryContent = new StringBuilder(1000);
        queryContent.append(String.format(
                "<%s> a rmo:PickMergeCommit, rmo:MergeCommit, rmo:Commit; "
                        + "	prov:wasAssociatedWith <%s> ;"
                        + "	dc-terms:title \"%s\" ;"
                        + "	prov:atTime \"%s\"^^xsd:dateTime ; %n"
                        + " rmo:usedSourceRevision <%s> ;"
                        + " rmo:usedSourceBranch <%s> ;"
                        + " rmo:usedTargetRevision <%s> ;"
                        + " rmo:usedTargetBranch <%s> .",
                commitURI, personUri, getMessage(), getTimeStamp(),
                usedSourceRevision.getRevisionURI(), usedSourceBranch.getReferenceURI(),
                usedTargetRevision.getRevisionURI(), usedTargetBranch.getReferenceURI()));

        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } }", getRevisionGraph().getRevisionGraphUri(),
                queryContent.toString());

        getTripleStoreInterface().executeUpdateQuery(query);

        // Copy the used target branch content to the used source branch
        fullGraphCopy(usedTargetBranch.getReferenceURI(), usedSourceBranch.getReferenceURI());

        // Get a path with all
//        usedSourceBranch.get

        Revision revisionToCopy = null;
        Revision generatedRevision = null;

//        while



//        updateBelongsTo(usedTargetBranch.getReferenceURI(), getPathCalculationInterface().getPathBetweenStartAndTargetRevision(getRevisionGraph(), usedTargetRevision, usedSourceRevision));
//        // Update the full graph of the target branch
//        fullGraphCopy(getRevisionGraph().getFullGraphUri(usedSourceBranch.getReferenceURI()), getRevisionGraph().getFullGraphUri(usedTargetBranch.getReferenceURI()));

        // Move source branch to new revision
        moveBranchReference(getRevisionGraph().getRevisionGraphUri(), usedSourceBranch.getReferenceURI(), revisionToCopy.getRevisionURI(), generatedRevision.getRevisionURI());
        // Update the source branch object
        usedSourceBranch = getRevisionGraph().getBranch(getBranchNameFrom(), true);

        return new PickMergeCommit(getRevisionGraph(), commitURI, getUser(), getTimeStamp(), getMessage(),
                usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch, null,
                null, false, null, null);
    }


    /**
     * Copies a revision and adds meta information to the revision graph.
     *
     * @param revisionToCopy the original revision to copy
     * @param sourceBranch the source branch
     * @param commitURL the associated commit URI
     * @return the generated revision
     */
    private Revision copyRevisionToTargetBranch(Revision revisionToCopy, Revision derivedFromRevision, Branch sourceBranch, String commitURL) throws InternalErrorException {

        String addSetContent = revisionToCopy.getAddSetContent();
        String deleteSetContent = revisionToCopy.getDeleteSetContent();

        RevisionDraft revisionDraft = new RevisionDraft(getRevisionManagement(), getRevisionGraph(), derivedFromRevision.getRevisionIdentifier(), addSetContent, deleteSetContent);
        Revision generatedRevision = revisionDraft.createRevisionInTripleStore();

        // Create the corresponding meta data
        StringBuilder queryContentInsert = new StringBuilder(1000);
        queryContentInsert.append(String.format(
                "<%1$s> prov:wasDerivedFrom <%2$s>; "
                        + "	prov:wasQuotedFrom <%3$s> ;"
                        + "	rmo:belongsTo <%4$s> ."
                + "<%5$s> prov:generated <%1$s> . ",
                generatedRevision.getRevisionURI(), derivedFromRevision.getRevisionURI(), revisionToCopy.getRevisionURI(),
                sourceBranch.getReferenceURI(), commitURL));

        StringBuilder queryContentDelete = new StringBuilder(1000);
        queryContentDelete.append(String.format(
                "<%1$s> rmo:belongsTo <%2$s> . ",
                revisionToCopy.getRevisionURI(), sourceBranch.getReferenceURI()));

        String query = Config.prefixes	+ String.format(""
                        + "DELETE DATA { GRAPH <%1$s> { %2$s } };"
                        + "INSERT DATA { GRAPH <%1$s> { %3$s } }",
                getRevisionGraph().getRevisionGraphUri(), queryContentDelete.toString(), queryContentInsert.toString());
        getTripleStoreInterface().executeUpdateQuery(query);

        return generatedRevision;
    }























}




// FROM INTERFACE

//    /**
//     *
//     * @param commit
//     * @return
//     * @throws InternalErrorException
//     */
//    public static MergeResult mergeRebase(final R43plesMergeCommit commit) throws InternalErrorException {
//
//        RevisionGraph graph = new RevisionGraph(commit.graphName);
//        String revisionGraph = graph.getRevisionGraphUri();
//        Revision revisionA = graph.getRevision(commit.branchNameA);
//        Revision revisionB = graph.getRevision(commit.branchNameB);
//        String revisionUriA = revisionA.getRevisionURI();
//        String revisionUriB = revisionB.getRevisionURI();
//
//        MergeResult mresult = new MergeResult(commit);
//
//        RebaseControl rebaseControl =  new RebaseControl(commit);
//        rebaseControl.checkIfRebaseIsPossible();
//
//        // Differ between MERGE query with specified SDD and without SDD
//        String usedSDDURI = graph.getSDD(commit.sdd);
//
//        // Get the common revision with shortest path
//        //TODO change when restructured to interface - only test of interface design
//        String commonRevision = PathCalculationSingleton.getInstance()
//                .getCommonRevisionWithShortestPath(graph, revisionA, revisionB)
//                .getRevisionURI();
//
////		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA,
////				revisionUriB);
//
//        // create the patch and patch group
//        LinkedList<String> revisionList = MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph,
//                commonRevision, revisionUriA);
//
//        rebaseControl.createPatchGroupOfBranch(revisionGraph, revisionUriB, revisionList);
//
//        // Create the revision progress for A and B
//        String graphNameA = commit.graphName + "-RM-REVISION-PROGRESS-A";
//        String graphNameB = commit.graphName + "-RM-REVISION-PROGRESS-B";
//        String graphNameDiff = commit.graphName + "-RM-DIFFERENCE-MODEL";
//        String uriA = "http://eatld.et.tu-dresden.de/branch-A";
//        String uriB = "http://eatld.et.tu-dresden.de/branch-B";
//
//        //TODO Change parameter to Path
////		MergeManagement.createRevisionProgresses(revisionGraph, commit.graphName,
////				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriA),
////				graphNameA, uriA,
////				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriB),
////				graphNameB, uriB);
//
//        // Create difference model
//        //TODO this method is provided by MergeCommitDraft
//        //MergeManagement.createDifferenceTripleModel(commit.graphName, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
//        //		usedSDDURI);
//
//        if ((commit.type != null) && (commit.type.equalsIgnoreCase("AUTO")) && !commit.with) {
//            logger.info("AUTO REBASE query detected");
//            // Create the merged revision
//            ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
//                    usedSDDURI, MergeQueryTypeEnum.AUTO);
//            String addedAsNTriples = addedAndRemovedTriples.get(0);
//            String removedAsNTriples = addedAndRemovedTriples.get(1);
//
//            String basisRevisionNumber = rebaseControl.forceRebaseProcess();
//            //TODO This will change because of the new Commit classes
////			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,	basisRevisionNumber);
//            mresult.graphStrategy = "auto-rebase";
//        } else if ((commit.type != null) && (commit.type.equalsIgnoreCase("MANUAL")) && !commit.with) {
//            logger.info("MANUAL REBASE query detected");
//            // Create the merged revision
//            ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
//                    usedSDDURI, MergeQueryTypeEnum.MANUAL);
//            String addedAsNTriples = addedAndRemovedTriples.get(0);
//            String removedAsNTriples = addedAndRemovedTriples.get(1);
//
//            String basisRevisionNumber = rebaseControl.forceRebaseProcess();
//            //TODO This will change because of the new Commit classes
////			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
////					basisRevisionNumber);
//            mresult.graphStrategy = "manual-rebase";
//        } else if ((commit.type == null) && commit.with) {
//            logger.info("REBASE WITH query detected");
//            // Create the merged revision -- newTriples
//            ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
//                    usedSDDURI, MergeQueryTypeEnum.WITH);
//            String addedAsNTriples = addedAndRemovedTriples.get(0);
//            String removedAsNTriples = addedAndRemovedTriples.get(1);
//
//            String basisRevisionNumber = rebaseControl.forceRebaseProcess();
//            //TODO This will change because of the new Commit classes
////			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
////					basisRevisionNumber);
//
//            mresult.graphStrategy = "with-rebase";
//        } else if ((commit.type == null) && !commit.with) {
//            logger.info("COMMON REBASE query detected");
//            // Check if difference model contains conflicts
//            String queryASK = String.format("ASK { %n" + "	GRAPH <%s> { %n"
//                    + " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
//                    + "	} %n" + "}", graphNameDiff);
//            if (TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK)) {
//                mresult.hasConflict = true;
//            } else {
//                rebaseControl.forceRebaseProcess();
//            }
//            mresult.conflictModel = RevisionManagementOriginal.getContentOfGraph(graphNameDiff, commit.format);
//
//        } else {
//            throw new InternalErrorException("This is not a valid MERGE query");
//        }
//
//        return mresult;
//    }





// FROM MERGEMANAGEMENT

//    /**
//     * Create a rebase merged revision.
//     *
//     //	 * @param graphName the graph name
//     //	 * @param branchNameA the name of branch A
//     //	 * @param branchNameB the name of branch B
//     //	 * @param user the user
//     //	 * @param commitMessage the commit message
//     //	 * @param graphNameDifferenceTripleModel the graph name of the difference triple model
//     //	 * @param graphNameRevisionProgressA the graph name of the revisions progress A
//     //	 * @param uriA the URI A
//     //	 * @param graphNameRevisionProgressB the graph name of the revisions progress B
//     //	 * @param uriB the URI B
//     //	 * @param uriSDD the URI of the SDD
//     //	 * @param type the merge query type
//     //	 * @param triples the triples which are belonging to the current merge query in N-Triple serialization
//     * @return new revision number
//     * @throws InternalErrorException
//     */
//    public static ArrayList<String> createRebaseMergedTripleList(final R43plesMergeCommit commit, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD, MergeQueryTypeEnum type) throws InternalErrorException {
//
//        RevisionGraph graph = new RevisionGraph(commit.graphName);
//
//        //set the triple list
//        ArrayList<String> list = new ArrayList<String>();
//
//        // Create an empty temporary graph which will contain the merged full content
//        String graphNameOfMerged = commit.graphName + "-RM-MERGED-TEMP";
//        TripleStoreInterfaceSingleton.get().executeCreateGraph(graphNameOfMerged);
//
//        // Get the full graph name of branch A
//        String graphNameOfBranchA = graph.getReferenceGraph(commit.branchNameA);
//        // Get the full graph name of branch B
//        String graphNameOfBranchB = graph.getReferenceGraph(commit.branchNameB);
//
//        logger.info("the triples: "+ commit.triples);
//        if (type.equals(MergeQueryTypeEnum.MANUAL)) {
//            // Manual merge query
//            RevisionManagementOriginal.executeINSERT(graphNameOfMerged, commit.triples);
//        } else {
//            // Copy graph B to temporary merged graph
//            String queryCopy = String.format("COPY <%s> TO <%s>", graphNameOfBranchB, graphNameOfMerged);
//            TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryCopy);
//
//            // Get the triples from branch A which should be added to/removed from the merged revision
//            String triplesToAdd = "";
//            String triplesToDelete = "";
//
//            // Get all difference groups
//            String queryDifferenceGroup = prefixes + String.format(
//                    "SELECT ?differenceCombinationURI ?automaticResolutionState ?tripleStateA ?tripleStateB ?conflict %n"
//                            + "WHERE { GRAPH <%s> { %n"
//                            + "	?differenceCombinationURI a rpo:DifferenceGroup ; %n"
//                            + "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
//                            + "		sddo:hasTripleStateA ?tripleStateA ; %n"
//                            + "		sddo:hasTripleStateB ?tripleStateB ; %n"
//                            + "		sddo:isConflicting ?conflict . %n"
//                            + "} }", graphNameDifferenceTripleModel);
//
//            // Iterate over all difference groups
//            ResultSet resultSetDifferenceGroups = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifferenceGroup);
//            while (resultSetDifferenceGroups.hasNext()) {
//                QuerySolution qsCurrentDifferenceGroup = resultSetDifferenceGroups.next();
//
//                String currentDifferencGroupURI = qsCurrentDifferenceGroup.getResource("?differenceCombinationURI").toString();
//                String currentDifferencGroupAutomaticResolutionState = qsCurrentDifferenceGroup.getResource("?automaticResolutionState").toString();
////				Currently not needed
////				String currentDifferencGroupTripleStateA = qsCurrentDifferenceGroup.getResource("?tripleStateA").toString();
////				String currentDifferencGroupTripleStateB = qsCurrentDifferenceGroup.getResource("?tripleStateB").toString();
//                boolean currentDifferencGroupConflict = qsCurrentDifferenceGroup.getLiteral("?conflict").getBoolean();
//
//                // Get all differences (triples) of current difference group
//                String queryDifference = prefixes + String.format(
//                        "SELECT ?s ?p ?o %n"
//                                + "WHERE { GRAPH <%s> { %n"
//                                + "	<%s> a rpo:DifferenceGroup ; %n"
//                                + "		rpo:hasDifference ?blankDifference . %n"
//                                + "	?blankDifference a rpo:Difference ; %n"
//                                + "		rpo:hasTriple ?triple . %n"
//                                + "	?triple rdf:subject ?s . %n"
//                                + "	?triple rdf:predicate ?p . %n"
//                                + "	?triple rdf:object ?o . %n"
//                                + "} }", graphNameDifferenceTripleModel, currentDifferencGroupURI);
//
//                // Iterate over all differences (triples)
//                ResultSet resultSetDifferences = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifference);
//                while (resultSetDifferences.hasNext()) {
//                    QuerySolution qsCurrentDifference = resultSetDifferences.next();
//
//                    String subject = "<" + qsCurrentDifference.getResource("?s").toString() + ">";
//                    String predicate = "<" + qsCurrentDifference.getResource("?p").toString() + ">";
//
//                    // Differ between literal and resource
//                    String object = "";
//                    if (qsCurrentDifference.get("?o").isLiteral()) {
//                        object = "\"" + qsCurrentDifference.getLiteral("?o").toString() + "\"";
//                    } else {
//                        object = "<" + qsCurrentDifference.getResource("?o").toString() + ">";
//                    }
//
//                    if (	type.equals(MergeQueryTypeEnum.AUTO) ||
//                            type.equals(MergeQueryTypeEnum.COMMON) ||
//                            (type.equals(MergeQueryTypeEnum.WITH) && !currentDifferencGroupConflict) ) {
//
//                        // MERGE AUTO or common MERGE query
//                        if (currentDifferencGroupAutomaticResolutionState.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
//                            // Triple should be added
//                            triplesToAdd += subject + " " + predicate + " " + object + " . \n";
//                        } else {
//                            // Triple should be deleted
//                            triplesToDelete += subject + " " + predicate + " " + object + " . \n";
//                        }
//                    }else {
//
//                        // MERGE WITH query - conflicting triple
//                        Model model = JenaModelManagement.readNTripleStringToJenaModel(commit.triples);
//                        // Create ASK query which will check if the model contains the specified triple
//                        String queryAsk = String.format(
//                                "ASK { %n"
//                                        + " %s %s %s %n"
//                                        + "}", subject, predicate, object);
//                        Query query = QueryFactory.create(queryAsk);
//                        QueryExecution qe = QueryExecutionFactory.create(query, model);
//                        boolean resultAsk = qe.execAsk();
//                        qe.close();
//                        model.close();
//                        if (resultAsk) {
//                            // Model contains the specified triple
//                            // Triple should be added
//                            triplesToAdd += subject + " " + predicate + " " + object + " . \n";
//                        } else {
//                            // Triple should be deleted
//                            triplesToDelete += subject + " " + predicate + " " + object + " . \n";
//                        }
//                    }
//                }
//                // Update the merged graph
//                // Insert triplesToAdd
//                RevisionManagementOriginal.executeINSERT(graphNameOfMerged, triplesToAdd);
//                // Delete triplesToDelete
//                RevisionManagementOriginal.executeDELETE(graphNameOfMerged, triplesToDelete);
//            }
//        }
//
//        // Calculate the add and delete sets
//
//        // Get all added triples (concatenate all triples which are in MERGED but not in A and all triples which are in MERGED but not in B)
//        String queryAddedTriples = String.format(
//                "CONSTRUCT {?s ?p ?o} %n"
//                        + "WHERE { %n"
//                        + "	GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	FILTER NOT EXISTS { %n"
//                        + "		GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	} %n"
//                        + "}", graphNameOfMerged, graphNameOfBranchA);
//
//        String addedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
//
//        queryAddedTriples = String.format(
//                "CONSTRUCT {?s ?p ?o} %n"
//                        + "WHERE { %n"
//                        + "	GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	FILTER NOT EXISTS { %n"
//                        + "		GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	} %n"
//                        + "}", graphNameOfMerged, graphNameOfBranchB);
//
//        addedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
//
//        // Get all removed triples (concatenate all triples which are in A but not in MERGED and all triples which are in B but not in MERGED)
//        String queryRemovedTriples = String.format(
//                "CONSTRUCT {?s ?p ?o} %n"
//                        + "WHERE { %n"
//                        + "	GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	FILTER NOT EXISTS { %n"
//                        + "		GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	} %n"
//                        + "}", graphNameOfBranchA, graphNameOfMerged);
//
//        String removedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);
//
//        queryRemovedTriples = String.format(
//                "CONSTRUCT {?s ?p ?o} %n"
//                        + "WHERE { %n"
//                        + "	GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	FILTER NOT EXISTS { %n"
//                        + "		GRAPH <%s> { ?s ?p ?o } %n"
//                        + "	} %n"
//                        + "}", graphNameOfBranchB, graphNameOfMerged);
//
//        removedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);
//
//        // Add the string to the result list
//        list.add(String.format(addedTriples));
//        list.add(String.format(removedTriples));
//
//        return list;
//    }


// FROM REBASECONTROL

//package de.tud.plt.r43ples.merging;
//
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.Map.Entry;
//
//import de.tud.plt.r43ples.management.RevisionManagementOriginal;
//import org.apache.log4j.Logger;
//
//import de.tud.plt.r43ples.exception.InternalErrorException;
//import de.tud.plt.r43ples.management.R43plesMergeCommit;
//import de.tud.plt.r43ples.existentobjects.RevisionGraph;
//import de.tud.plt.r43ples.merging.management.StrategyManagement;
//import de.tud.plt.r43ples.merging.model.structure.Patch;
//import de.tud.plt.r43ples.merging.model.structure.PatchGroup;
//
//public class RebaseControl {
//
//	/** The logger. **/
//	private static Logger logger = Logger.getLogger(RebaseControl.class);
//	private String graphName;
//	private String branchNameB;
//	private String branchNameA;
//	private PatchGroup patchGroup;
//
//
//
//
//	public RebaseControl(final String graphName, final String branchNameA, final String branchNameB) {
//		this.graphName = graphName;
//		this.branchNameA = branchNameA;
//		this.branchNameB = branchNameB;
//	}
//
//	public RebaseControl(final R43plesMergeCommit commit) {
//		this.graphName = commit.graphName;
//		this.branchNameA = commit.branchNameA;
//		this.branchNameB = commit.branchNameB;
//	}
//
//	/** simple checks if rebase could be possible for these two branches of a graph
//	 *
//	 * @throws InternalErrorException throws an error if it is not possible
//	 */
//	public void checkIfRebaseIsPossible() throws InternalErrorException {
//		// Check if graph already exists
//		if (!RevisionManagementOriginal.checkGraphExistence(graphName)){
//			logger.error("Graph <"+graphName+"> does not exist.");
//			throw new InternalErrorException("Graph <"+graphName+"> does not exist.");
//		}
//
//		RevisionGraph graph = new RevisionGraph(graphName);
//		// Check if A and B are different revisions
//		if (graph.getRevisionIdentifier(branchNameA).equals(graph.getRevisionIdentifier(branchNameB))) {
//			// Branches are equal - throw error
//			throw new InternalErrorException("Specified branches are equal");
//		}
//
//		// Check if both are terminal nodes
//		if (!(graph.hasBranch(branchNameA) && graph.hasBranch(branchNameB))) {
//			throw new InternalErrorException("Non terminal nodes were used ");
//		}
//	}
//
//
//	/**for each revision in branchA , create a patch */
//	public PatchGroup createPatchGroupOfBranch(String revisionGraph, String basisRevisionUri, LinkedList<String> revisionList) {
//
//		LinkedHashMap<String, Patch> patchMap = new LinkedHashMap<String, Patch>();
//
//		Iterator<String> rIter  = revisionList.iterator();
//
//		while(rIter.hasNext()) {
//			String revisionUri = rIter.next();
//			String commitUri = StrategyManagement.getCommitUri(revisionGraph, revisionUri);
//
//			String addSet = RevisionManagementOriginal.getAddSetURI(revisionUri, revisionGraph);
//			String deleteSet = RevisionManagementOriginal.getDeleteSetURI(revisionUri, revisionGraph);
//
//			String patchNumber = StrategyManagement.getRevisionNumber(revisionGraph, revisionUri);
//			String patchUser = StrategyManagement.getCommitUserUri(revisionGraph, commitUri);
//			String patchMessage = StrategyManagement.getCommitMessage(revisionGraph, commitUri);
//
//			patchMap.put(patchNumber, new Patch(patchNumber, patchUser, patchMessage, addSet, deleteSet));
//		}
//
//		String basisRevisionNumber = StrategyManagement.getRevisionNumber(revisionGraph, basisRevisionUri);
//
//		patchGroup = new PatchGroup(basisRevisionNumber, patchMap);
//
//		logger.debug("patchGroup initial successful!" + patchGroup.getPatchMap().size());
//		return patchGroup;
//	}
//
//
//	/**
//	 * force rebase begin, for each patch in patch group will a new revision created
//	 * @throws InternalErrorException
//	 * */
//	public String forceRebaseProcess() throws InternalErrorException{
//
//		logger.debug("patchGroup 1:" + patchGroup.getBasisRevisionNumber());
//		logger.debug("patchGroup 2:" + patchGroup.getPatchMap().size());
//
//		LinkedHashMap<String, Patch> patchMap = patchGroup.getPatchMap();
//		String basisRevisionNumber = patchGroup.getBasisRevisionNumber();
//
//		Iterator<Entry<String, Patch>> pIter = patchMap.entrySet().iterator();
//
//		while(pIter.hasNext()) {
//			Entry<String, Patch> pEntry = pIter.next();
//			Patch patch = pEntry.getValue();
//
//			String newRevisionNumber = createNewRevisionWithPatch(
//					graphName, patch.getAddedSetUri(), patch.getRemovedSetUri(),
//					patch.getPatchUser(), patch.getPatchMessage(), basisRevisionNumber);
//
//			basisRevisionNumber = newRevisionNumber;
//		}
//		return basisRevisionNumber;
//	}
//
//	/**
//	 * create new revision with patch with addedUri and removedUri
//	 *
//	 * @param graphName
//	 *            the graph name
//	 * @param addSetGraphUri
//	 *           uri of the data set of added triples as N-Triples
//	 * @param deleteSetGraphUri
//	 *           uri of the data set of removed triples as N-Triples
//	 * @param user
//	 *            the user name who creates the revision
//	 * @param commitMessage
//	 *            the title of the revision
//	 * @param usedRevisionNumber
//	 *            the number of the revision which is used for creation of the
//	 *            new revision
//	 * @return new revision number
//	 * @throws InternalErrorException
//	 */
//	public static String createNewRevisionWithPatch(final String graphName, final String addSetGraphUri, final String deleteSetGraphUri,
//													final String user, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {
//
//		// TODO currently not working
////		RevisionDraft d = new RevisionDraft(graphName, usedRevisionNumber);
////		d.addSetURI = addSetGraphUri;
////		d.deleteSetURI = deleteSetGraphUri;
////		addNewRevisionFromChangeSet(user, commitMessage, d);
////		return d.newRevisionNumber;
//		return null;
//	}
//
//}
