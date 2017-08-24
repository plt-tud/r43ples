package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.management.R43plesRequest;

/**
 * Collection of information for creating a new rebase merge commit.
 *
 * @author Stephan Hensel
 */
public class RebaseMergeCommitDraft extends MergeCommitDraft {


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public RebaseMergeCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
    }

    // TODO

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