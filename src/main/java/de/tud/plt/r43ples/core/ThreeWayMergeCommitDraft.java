package de.tud.plt.r43ples.core;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.ThreeWayMergeCommit;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.optimization.ChangeSetPath;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of information for creating a new three way merge commit.
 *
 * @author Stephan Hensel
 */
public class ThreeWayMergeCommitDraft extends MergeCommitDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(ThreeWayMergeCommitDraft.class);

    /** The used source branch. **/
    private Branch usedSourceBranch;
    /** The used target branch. **/
    private Branch usedTargetBranch;


    /**
     * The constructor.
     * Creates a three way merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    protected ThreeWayMergeCommitDraft(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        super(graphName, branchNameFrom, branchNameInto, user, message, MergeActions.MERGE, triples, type, with);
        this.usedSourceBranch = getRevisionGraph().getBranch(getBranchNameFrom(), true);
        this.usedTargetBranch = getRevisionGraph().getBranch(getBranchNameInto(), true);
    }

    /**
     * Tries to create a new commit draft as a new commit in the triple store.
     * If possible it will create the corresponding revision and the meta data.
     *
     * @return the commit (has attribute which indicates if the commit was executed or not)
     */
    protected ThreeWayMergeCommit createCommitInTripleStore() throws InternalErrorException {

        Revision fromRevision = this.usedSourceBranch.getLeafRevision();

        Revision intoRevision = this.usedTargetBranch.getLeafRevision();

        // Get the default SDG URI of the revision graph
        String usedSDDURI = getRevisionGraph().getSDG();

        // Get the common revision with shortest path
        Revision commonRevision = this.getPathCalculationInterface().getCommonRevisionWithShortestPath(fromRevision, intoRevision);

        // Create the revision progress for from and into
        String namedGraphUriFrom = getUriCalculator().getTemporaryRevisionProgressFromURI(getRevisionGraph());
        String namedGraphUriInto = getUriCalculator().getTemporaryRevisionProgressIntoURI(getRevisionGraph());
        String namedGraphUriDiff = getUriCalculator().getTemporaryDifferenceModelURI(getRevisionGraph());
        String uriA = "http://eatld.et.tu-dresden.de/branch-from";
        String uriB = "http://eatld.et.tu-dresden.de/branch-into";

        ChangeSetPath changesetsFromCommontoSourceBranch = this.getPathCalculationInterface().getChangeSetsBetweenStartAndTargetRevision(commonRevision, fromRevision);
        ChangeSetPath changesetsFromCommontoTargetBranch = this.getPathCalculationInterface().getChangeSetsBetweenStartAndTargetRevision(commonRevision, intoRevision);


        createRevisionProgresses(
                changesetsFromCommontoSourceBranch, namedGraphUriFrom, uriA,
                changesetsFromCommontoTargetBranch, namedGraphUriInto, uriB,
                commonRevision);

        // Create difference model
        createDifferenceTripleModel(namedGraphUriDiff, namedGraphUriFrom, uriA, namedGraphUriInto, uriB,
                usedSDDURI);

        // The created revision
        Revision revision;

        // Differ between the different merge queries
        if ((getType() != null) && (getType().equals(MergeTypes.AUTO)) && !isWith()) {
            logger.debug("AUTO MERGE query detected");
            // Create the merged revision
            revision = createMergedRevision(namedGraphUriDiff, MergeQueryTypeEnum.AUTO, fromRevision);
            return addMetaInformation(revision, namedGraphUriDiff, commonRevision, fromRevision, intoRevision);
        } else if ((getType() != null) && (getType().equals(MergeTypes.MANUAL)) && isWith()) {
            logger.debug("MANUAL MERGE query detected");
            // Create the merged revision
            revision = createMergedRevision(namedGraphUriDiff, MergeQueryTypeEnum.MANUAL, fromRevision);
            return addMetaInformation(revision, namedGraphUriDiff, commonRevision, fromRevision, intoRevision);
        } else if ((getType() == null) && isWith()) {
            logger.debug("MERGE WITH query detected");
            // Create the merged revision
            revision = createMergedRevision(namedGraphUriDiff, MergeQueryTypeEnum.WITH, fromRevision);
            return addMetaInformation(revision, namedGraphUriDiff, commonRevision, fromRevision, intoRevision);
        } else if ((getType() == null) && !isWith()) {
            logger.debug("MERGE query detected");
            // Check if difference model contains conflicts
            String queryASK = String.format("ASK { %n" + "	GRAPH <%s> { %n"
                    + " 	?ref <http://eatld.et.tu-dresden.de/mmo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
                    + "	} %n" + "}", namedGraphUriDiff);
            if (getTripleStoreInterface().executeAskQuery(queryASK)) {
                // Difference model contains conflicts
                // Return the conflict model to the client
                String conflictModel = Helper.getContentOfGraph(namedGraphUriDiff, "text/turtle");
                return new ThreeWayMergeCommit(getRevisionGraph(), null,null, null, null, fromRevision, null, intoRevision, null, null, null, true, conflictModel, namedGraphUriDiff);
            } else {
                // Difference model contains no conflicts
                // Create the merged revision
                revision = createMergedRevision(namedGraphUriDiff, MergeQueryTypeEnum.COMMON, fromRevision);
                return addMetaInformation(revision, namedGraphUriDiff, commonRevision, fromRevision, intoRevision);
            }
        } else {
            throw new InternalErrorException("This is not a valid MERGE query");
        }
    }

    /**
     * Adds meta information for commit and revision to the revision graph.
     *
     * <img src="{@docRoot}../../doc/revision management description/r43ples-threewaymerge.png" />
     *
     * @param generatedRevision the generated revision
     * @param namedGraphUriDiff the named graph URI of the difference model
     * @param commonRevision the common revision of this merge
     * @param usedSourceRevision the used source revision (from)
     * @param usedTargetRevision the used target revision (into)
     * @return the created commit
     * @throws InternalErrorException
     */
    private ThreeWayMergeCommit addMetaInformation(Revision generatedRevision, String namedGraphUriDiff, Revision commonRevision, Revision usedSourceRevision, Revision usedTargetRevision) throws InternalErrorException {

        String commitURI = getUriCalculator().getNewThreeWayMergeCommitURI(getRevisionGraph(), generatedRevision.getRevisionIdentifier());;

        String personUri = Helper.getUserURI(getUser());

        // Create a new commit (activity)
        StringBuilder queryContent = new StringBuilder(1000);
        queryContent.append(String.format(
                "<%s> a rmo:ThreeWayMergeCommit, rmo:MergeCommit, rmo:BasicMergeCommit, rmo:Commit; "
                        + "	rmo:wasAssociatedWith <%s> ;"
                        + "	rmo:commitMessage \"%s\" ;"
                        + "	rmo:timeStamp \"%s\"^^xsd:dateTime ; %n"
                        + " rmo:generated <%s> ;"
                        + " rmo:hasChangeSet <%s> ;"
                        + " rmo:hasChangeSet <%s> ;"
                        + " rmo:usedSourceRevision <%s> ;"
                        + " rmo:usedSourceBranch <%s> ;"
                        + " rmo:usedTargetRevision <%s> ;"
                        + " rmo:usedTargetBranch <%s> .",
                commitURI, personUri, getMessage(), getTimeStamp(),
                generatedRevision.getRevisionURI(), generatedRevision.getChangeSets().get(0).getChangeSetURI(), generatedRevision.getChangeSets().get(1).getChangeSetURI(), usedSourceRevision.getRevisionURI(), usedSourceBranch.getReferenceURI(),
                usedTargetRevision.getRevisionURI(), usedTargetBranch.getReferenceURI()));

        // Create revision meta data for additional change set
        queryContent.append(String.format(
                  "<%1$s> rmo:succeedingRevision <%2$s> . %n"
                + "<%2$s> rmo:wasDerivedFrom <%3$s> .",
                generatedRevision.getChangeSets().get(1).getChangeSetURI(), generatedRevision.getRevisionURI(), usedSourceRevision.getRevisionURI()));

        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } }", getRevisionGraph().getRevisionGraphUri(),
                queryContent.toString());

        getTripleStoreInterface().executeUpdateQuery(query);

        // Move branch to new revision
        moveBranchReference(getRevisionGraph().getRevisionGraphUri(), usedTargetBranch.getReferenceURI(), usedTargetRevision.getRevisionURI(), generatedRevision.getRevisionURI());
        updateReferencedFullGraph(usedTargetBranch.getFullGraphURI(), generatedRevision.getChangeSets().get(0));
        // Update the target branch object
        usedTargetBranch = getRevisionGraph().getBranch(getBranchNameInto(), true);

        return new ThreeWayMergeCommit(getRevisionGraph(), commitURI, getUser(), getTimeStamp(), getMessage(),
                usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch, generatedRevision,
                commonRevision, false, null, namedGraphUriDiff);
    }

    /**
     * Create a merged revision with meta data
     *
     * @param graphNameDifferenceTripleModel the graph name of the difference triple model
     * @param type the merge query type
     * @param usedSourceRevision the used source revision
     * @return the created revision
     * @throws InternalErrorException
     */
    private Revision createMergedRevision(String graphNameDifferenceTripleModel, MergeQueryTypeEnum type, Revision usedSourceRevision) throws InternalErrorException {

        // Create an empty temporary graph which will contain the merged full content
        String graphNameOfMerged = getUriCalculator().getTemporaryMergedURI(getRevisionGraph());
        getTripleStoreInterface().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameOfMerged));
        getTripleStoreInterface().executeCreateGraph(graphNameOfMerged);

        // Get the full graph name of branch A
        String graphNameOfBranchA = usedSourceBranch.getFullGraphURI();
        // Get the full graph name of branch B
        String graphNameOfBranchB = usedTargetBranch.getFullGraphURI();

        if (type.equals(MergeQueryTypeEnum.MANUAL)) {
            // Manual merge query
            Helper.executeINSERT(graphNameOfMerged, getTriples());
        } else {
            // Copy graph B to temporary merged graph
            String queryCopy = String.format("COPY <%s> TO <%s>", graphNameOfBranchB, graphNameOfMerged);
            getTripleStoreInterface().executeUpdateQuery(queryCopy);

            // Get the triples from branch A which should be added to/deleted from the merged revision
            String triplesToAdd = "";
            String triplesToDelete = "";

            // Get all difference groups
            String queryDifferenceGroup = Config.prefixes + String.format(
                    "SELECT ?differenceCombinationURI ?automaticResolutionState ?tripleStateA ?tripleStateB ?conflict %n"
                            + "WHERE { GRAPH <%s> { %n"
                            + "	?differenceCombinationURI a mmo:DifferenceGroup ; %n"
                            + "		mmo:automaticResolutionState ?automaticResolutionState ; %n"
                            + "		mmo:hasTripleStateA ?tripleStateA ; %n"
                            + "		mmo:hasTripleStateB ?tripleStateB ; %n"
                            + "		mmo:isConflicting ?conflict . %n"
                            + "} }", graphNameDifferenceTripleModel);

            // Iterate over all difference groups
            ResultSet resultSetDifferenceGroups = getTripleStoreInterface().executeSelectQuery(queryDifferenceGroup);
            while (resultSetDifferenceGroups.hasNext()) {
                QuerySolution qsCurrentDifferenceGroup = resultSetDifferenceGroups.next();

                String currentDifferencGroupURI = qsCurrentDifferenceGroup.getResource("?differenceCombinationURI").toString();
                String currentDifferencGroupAutomaticResolutionState = qsCurrentDifferenceGroup.getResource("?automaticResolutionState").toString();
//				Currently not needed
//				String currentDifferencGroupTripleStateA = qsCurrentDifferenceGroup.getResource("?tripleStateA").toString();
//				String currentDifferencGroupTripleStateB = qsCurrentDifferenceGroup.getResource("?tripleStateB").toString();
                boolean currentDifferencGroupConflict = qsCurrentDifferenceGroup.getLiteral("?conflict").getBoolean();

                // Get all differences (triples) of current difference group
                String queryDifference = Config.prefixes + String.format(
                        "SELECT ?s ?p ?o %n"
                                + "WHERE { GRAPH <%s> { %n"
                                + "	<%s> a mmo:DifferenceGroup ; %n"
                                + "		mmo:hasDifference ?blankDifference . %n"
                                + "	?blankDifference a mmo:Difference ; %n"
                                + "		mmo:hasTriple ?triple . %n"
                                + "	?triple rdf:subject ?s . %n"
                                + "	?triple rdf:predicate ?p . %n"
                                + "	?triple rdf:object ?o . %n"
                                + "} }", graphNameDifferenceTripleModel, currentDifferencGroupURI);

                // Iterate over all differences (triples)
                ResultSet resultSetDifferences = getTripleStoreInterface().executeSelectQuery(queryDifference);
                while (resultSetDifferences.hasNext()) {
                    QuerySolution qsCurrentDifference = resultSetDifferences.next();

                    String subject = "<" + qsCurrentDifference.getResource("?s").toString() + ">";
                    String predicate = "<" + qsCurrentDifference.getResource("?p").toString() + ">";

                    // Differ between literal and resource
                    String object;
                    if (qsCurrentDifference.get("?o").isLiteral()) {
                        object = "\"" + qsCurrentDifference.getLiteral("?o").toString() + "\"";
                    } else {
                        object = "<" + qsCurrentDifference.getResource("?o").toString() + ">";
                    }

                    if (	type.equals(MergeQueryTypeEnum.AUTO) ||
                            type.equals(MergeQueryTypeEnum.COMMON) ||
                            (type.equals(MergeQueryTypeEnum.WITH) && !currentDifferencGroupConflict) ) {
                        // MERGE AUTO or common MERGE query
                        if (currentDifferencGroupAutomaticResolutionState.equals(MergeTripleStateEnum.ADDED.getSdRepresentation())) {
                            // Triple should be added
                            triplesToAdd += subject + " " + predicate + " " + object + " . \n";
                        } else {
                            // Triple should be deleted
                            triplesToDelete += subject + " " + predicate + " " + object + " . \n";
                        }
                    } else {
                        // MERGE WITH query - conflicting triple
                        Model model = JenaModelManagement.readNTripleStringToJenaModel(getTriples());
                        // Create ASK query which will check if the model contains the specified triple
                        String queryAsk = String.format(
                                "ASK { %n"
                                        + " %s %s %s %n"
                                        + "}", subject, predicate, object);
                        Query query = QueryFactory.create(queryAsk);
                        QueryExecution qe = QueryExecutionFactory.create(query, model);
                        boolean resultAsk = qe.execAsk();
                        qe.close();
                        model.close();
                        if (resultAsk) {
                            // Model contains the specified triple
                            // Triple should be added
                            triplesToAdd += subject + " " + predicate + " " + object + " . \n";
                        } else {
                            // Triple should be deleted
                            triplesToDelete += subject + " " + predicate + " " + object + " . \n";
                        }
                    }
                }
                // Update the merged graph
                // Insert triplesToAdd
                Helper.executeINSERT(graphNameOfMerged, triplesToAdd);
                // Delete triplesToDelete
                Helper.executeDELETE(graphNameOfMerged, triplesToDelete);
            }
        }

        // Calculate the add and delete sets

        // Get all added triples for both changesets
        String queryAddedTriplesA = String.format(
                "CONSTRUCT {?s ?p ?o} %n"
                        + "WHERE { %n"
                        + "	GRAPH <%s> { ?s ?p ?o } %n"
                        + "	FILTER NOT EXISTS { "
                        + "		GRAPH <%s> { ?s ?p ?o } %n"
                        + "	} %n"
                        + "}", graphNameOfMerged, graphNameOfBranchA);

        String addedTriplesA = getTripleStoreInterface().executeConstructQuery(queryAddedTriplesA, FileUtils.langNTriple);

        String queryAddedTriplesB = String.format(
                "CONSTRUCT {?s ?p ?o} %n"
                        + "WHERE { %n"
                        + "	GRAPH <%s> { ?s ?p ?o } %n"
                        + "	FILTER NOT EXISTS { %n"
                        + "		GRAPH <%s> { ?s ?p ?o } %n"
                        + "	} %n"
                        + "}", graphNameOfMerged, graphNameOfBranchB);

        String addedTriplesB = getTripleStoreInterface().executeConstructQuery(queryAddedTriplesB, FileUtils.langNTriple);

        // Get all deleted triples for both changesets
        String queryDeletedTriplesA = String.format(
                "CONSTRUCT {?s ?p ?o} %n"
                        + "WHERE { %n"
                        + "	GRAPH <%s> { ?s ?p ?o } %n"
                        + "	FILTER NOT EXISTS { %n"
                        + "		GRAPH <%s> { ?s ?p ?o } %n"
                        + "	} %n"
                        + "}", graphNameOfBranchA, graphNameOfMerged);

        String deletedTriplesA = getTripleStoreInterface().executeConstructQuery(queryDeletedTriplesA, FileUtils.langNTriple);

        String queryDeletedTriplesB = String.format(
                "CONSTRUCT {?s ?p ?o} %n"
                        + "WHERE { %n"
                        + "	GRAPH <%s> { ?s ?p ?o } %n"
                        + "	FILTER NOT EXISTS { %n"
                        + "		GRAPH <%s> { ?s ?p ?o } %n"
                        + "	} %n"
                        + "}", graphNameOfBranchB, graphNameOfMerged);

        String deletedTriplesB = getTripleStoreInterface().executeConstructQuery(queryDeletedTriplesB, FileUtils.langNTriple);

        // Create the merge revision and a change set
        RevisionDraft revisionDraft = new RevisionDraft(getUriCalculator(), getRevisionGraph(), usedTargetBranch,
                addedTriplesB, deletedTriplesB, false);
        Revision generatedRevision = revisionDraft.createInTripleStore();

        ChangeSetDraft changeSetDraftA = new ChangeSetDraft(getUriCalculator(), getRevisionGraph(),
                usedSourceBranch.getLeafRevision(), generatedRevision.getRevisionIdentifier(), generatedRevision.getRevisionURI(), usedSourceBranch.getReferenceURI(),
                addedTriplesA, deletedTriplesA, false, false);
        ChangeSet changeSetA = changeSetDraftA.createInTripleStore();

        generatedRevision.addChangeSet(changeSetA);
        return generatedRevision;
    }

    /**
     * Create the revision progresses for both branches.
     *
     * @param pathFrom the path with all revisions from start revision to target revision of the from branch
     * @param graphNameRevisionProgressFrom the graph name of the revision progress of the from branch
     * @param uriFrom the URI of the revision progress of the from branch
     * @param pathInto the linked list with all revisions from start revision to target revision of the into branch
     * @param graphNameRevisionProgressInto the graph name of the revision progress of the into branch
     * @param uriInto the URI of the revision progress of the into branch
     * @throws InternalErrorException
     */
    protected void createRevisionProgresses(ChangeSetPath pathFrom, String graphNameRevisionProgressFrom, String uriFrom,
                                            ChangeSetPath pathInto, String graphNameRevisionProgressInto, String uriInto,
                                            Revision commonRevision) throws InternalErrorException {
        logger.info("Create the revision progress of branch from and into.");

        if (!((pathFrom.getRevisionPath().size() > 0) && (pathInto.getRevisionPath().size() > 0))) {
            throw new InternalErrorException("Revision path contains no revisions.");
        }

        // Get the full graph name of common revision or create full revision graph of common revision
        FullGraph fullGraph = new FullGraph(this.getRevisionGraph(), commonRevision);

        // Create revision progress of branch from
        createRevisionProgress(fullGraph, pathFrom, graphNameRevisionProgressFrom, uriFrom);

        // Create revision progress of branch into
        createRevisionProgress(fullGraph, pathInto, graphNameRevisionProgressInto, uriInto);

        // Drop the temporary full graph
        fullGraph.purge();
    }

    /**
     * Create the revision progress.
     *
     * @param startingFullGraph the full graph name of the revision of path
     * @param path the path with all revisions from start revision to target revision
     * @param graphNameRevisionProgress the graph name where the revision progress should be stored
     * @param uri the URI where the revision progress should be stored
     * @throws InternalErrorException
     */
    protected void createRevisionProgress(FullGraph startingFullGraph, ChangeSetPath path, String graphNameRevisionProgress, String uri) throws InternalErrorException {
        logger.info("Create the revision progress of " + uri + " in graph " + graphNameRevisionProgress + ".");

        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameRevisionProgress));
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE GRAPH  <%s>", graphNameRevisionProgress));

        // Create the initial content
        logger.info("Create the initial content.");
        String queryInitial = Config.prefixes + String.format(
                "INSERT { GRAPH <%s> { %n"
                        + "	<%s> a mmo:RevisionProgress; %n"
                        + "		mmo:Original [ %n"
                        + "			rdf:subject ?s ; %n"
                        + "			rdf:predicate ?p ; %n"
                        + "			rdf:object ?o ; %n"
                        + "			rmo:references <%s> %n"
                        + "		] %n"
                        + "} } WHERE { %n"
                        + "	GRAPH <%s> %n"
                        + "		{ ?s ?p ?o . } %n"
                        + "}", graphNameRevisionProgress, uri, startingFullGraph.getRevision().getRevisionURI(), startingFullGraph.getFullGraphUri());

        // Execute the query which generates the initial content
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryInitial);

        // Update content by current add and delete set - remove old entries
        while (path.getRevisionPath().size() > 0) {
            ChangeSet changeSet = path.getRevisionPath().removeLast();
            logger.info("Update content by current add and delete set of changeset " + changeSet + " - remove old entries.");
            // Get the ADD and DELETE set URIs
            String addSetURI = changeSet.getAddSetURI();
            String deleteSetURI = changeSet.getDeleteSetURI();

            if ((addSetURI != null) && (deleteSetURI != null)) {

                // Update the revision progress with the data of the current revision ADD set

                // Delete old entries (original)
                String queryRevision = Config.prefixes + String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Original ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Original ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);

                queryRevision += "\n";

                // Delete old entries (added)
                queryRevision += String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Added ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Added ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);

                queryRevision += "\n";

                // Delete old entries (deleted)
                queryRevision += String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Deleted ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Deleted ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);

                queryRevision += "\n";

                // Insert new entries (added)
                queryRevision += String.format(
                        "INSERT { GRAPH <%s> {%n"
                                + "	<%s> a mmo:RevisionProgress; %n"
                                + "		mmo:Added [ %n"
                                + "			rdf:subject ?s ; %n"
                                + "			rdf:predicate ?p ; %n"
                                + "			rdf:object ?o ; %n"
                                + "			rmo:references <%s> %n"
                                + "		] %n"
                                + "} } WHERE { %n"
                                + "	GRAPH <%s> %n"
                                + "		{ ?s ?p ?o . } %n"
                                + "};", graphNameRevisionProgress, uri, changeSet.getChangeSetURI(), addSetURI);

                queryRevision += "\n \n";

                // Update the revision progress with the data of the current revision DELETE set

                // Delete old entries (original)
                queryRevision += String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Original ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Original ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);

                queryRevision += "\n";

                // Delete old entries (added)
                queryRevision += String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Added ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Added ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);

                queryRevision += "\n";

                // Delete old entries (deleted)
                queryRevision += String.format(
                        "DELETE { GRAPH <%s> { %n"
                                + "	<%s> mmo:Deleted ?blank . %n"
                                + "	?blank rdf:subject ?s . %n"
                                + "	?blank rdf:predicate ?p . %n"
                                + "	?blank rdf:object ?o . %n"
                                + "	?blank rmo:references ?revision . %n"
                                + "} } %n"
                                + "WHERE { "
                                + "		GRAPH <%s> { %n"
                                + "			<%s> mmo:Deleted ?blank . %n"
                                + "			?blank rdf:subject ?s . %n"
                                + "			?blank rdf:predicate ?p . %n"
                                + "			?blank rdf:object ?o . %n"
                                + "			?blank rmo:references ?revision . %n"
                                + "		} %n"
                                + "		GRAPH <%s> { %n"
                                + "			?s ?p ?o %n"
                                + "		} %n"
                                + "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);

                queryRevision += "\n";

                // Insert new entries (deleted)
                queryRevision += String.format(
                        "INSERT { GRAPH <%s> { %n"
                                + "	<%s> a mmo:RevisionProgress; %n"
                                + "		mmo:Deleted [ %n"
                                + "			rdf:subject ?s ; %n"
                                + "			rdf:predicate ?p ; %n"
                                + "			rdf:object ?o ; %n"
                                + "			rmo:references <%s> %n"
                                + "		] %n"
                                + "} } WHERE { %n"
                                + "	GRAPH <%s> %n"
                                + "		{ ?s ?p ?o . } %n"
                                + "}", graphNameRevisionProgress, uri, changeSet.getChangeSetURI(), deleteSetURI);

                // Execute the query which updates the revision progress by the current revision
                TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);

            } else {
                //TODO Error management - is needed when a ADD or DELETE set is not referenced in the current implementation this error should not occur
                logger.error("ADD or DELETE set of " + changeSet.getChangeSetURI() + "does not exists.");
            }
            logger.info("Revision progress was created.");
        }
    }

    /**
     * Create the difference triple model which contains all differing triples and store it in named graph
     *
     * @param graphNameDifferenceTripleModel the graph name where the generated difference triple model should be stored
     * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
     * @param uriA the URI of the revision progress of branch A
     * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
     * @param uriB the URI of the revision progress of branch B
     * @param uriSDG the URI of the SDG to use
     */
    private void createDifferenceTripleModel(String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDG) {

        logger.info("Create the difference triple model");
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameDifferenceTripleModel));
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE GRAPH  <%s>", graphNameDifferenceTripleModel));

        // Templates for revision A and B
        String sparqlTemplateRevisionA = String.format(
                "	GRAPH <%s> { %n"
                        + "		<%s> <%s> ?blankA . %n"
                        + "			?blankA rdf:subject ?s . %n"
                        + "			?blankA rdf:predicate ?p . %n"
                        + "			?blankA rdf:object ?o . %n"
                        + "			?blankA rmo:references ?revisionA . %n"
                        + "	} %n", graphNameRevisionProgressA, uriA, "%s");
        String sparqlTemplateRevisionB = String.format(
                "	GRAPH <%s> { %n"
                        + "		<%s> <%s> ?blankB . %n"
                        + "			?blankB rdf:subject ?s . %n"
                        + "			?blankB rdf:predicate ?p . %n"
                        + "			?blankB rdf:object ?o . %n"
                        + "			?blankB rmo:references ?revisionB . %n"
                        + "	} %n", graphNameRevisionProgressB, uriB, "%s");

        String sparqlTemplateNotExistsRevisionA = String.format(
                "FILTER NOT EXISTS { %n"
                        + "	GRAPH <%s> { %n"
                        + "		<%s> ?everything ?blankA . %n"
                        + "			?blankA rdf:subject ?s . %n"
                        + "			?blankA rdf:predicate ?p . %n"
                        + "			?blankA rdf:object ?o . %n"
                        + "			?blankA rmo:references ?revisionA . %n"
                        + "	} %n"
                        + "}", graphNameRevisionProgressA, uriA);

        String sparqlTemplateNotExistsRevisionB = String.format(
                "FILTER NOT EXISTS { %n"
                        + "	GRAPH <%s> { %n"
                        + "		<%s> ?everything ?blankB . %n"
                        + "			?blankB rdf:subject ?s . %n"
                        + "			?blankB rdf:predicate ?p . %n"
                        + "			?blankB rdf:object ?o . %n"
                        + "			?blankB rmo:references ?revisionB . %n"
                        + "	} %n"
                        + "}", graphNameRevisionProgressB, uriB);

        // Get all structural definitions which are generating differences
        String queryDifferingSD = String.format(
                "PREFIX mmo: <http://eatld.et.tu-dresden.de/mmo#> %n"
                        + "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> %n"
                        + "SELECT ?combinationURI ?tripleStateA ?tripleStateB ?conflict ?automaticResolutionState %n"
                        + "WHERE { GRAPH <%s> { %n"
                        + "	<%s> a mmo:StructuralDefinitionGroup ;"
                        + "		mmo:hasStructuralDefinition ?combinationURI ."
                        + "	?combinationURI a mmo:StructuralDefinition ; %n"
                        + "		mmo:hasTripleStateA ?tripleStateA ; %n"
                        + "		mmo:hasTripleStateB ?tripleStateB ; %n"
                        + "		mmo:isConflicting ?conflict ; %n"
                        + "		mmo:automaticResolutionState ?automaticResolutionState . %n"
                        + "} } %n", Config.sdg_graph, uriSDG);

        // Iterate over all differing combination URIs
        ResultSet resultSetDifferences = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifferingSD);
        while (resultSetDifferences.hasNext()) {
            QuerySolution qs = resultSetDifferences.next();

            String currentDifferenceCombinationURI = qs.getResource("?combinationURI").toString();
            String currentTripleStateA = qs.getResource("?tripleStateA").toString();
            String currentTripleStateB = qs.getResource("?tripleStateB").toString();
            // Will return an integer value because virtuoso stores boolean internal as integer
            String currentConflictState = qs.getLiteral("?conflict").toString();
            // TDB returns boolean value without "" -> add it to use it in the next query correctly
            if (currentConflictState.equals("true^^http://www.w3.org/2001/XMLSchema#boolean")) {
                currentConflictState = "\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
            } else {
                currentConflictState = "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
            }
            String currentAutomaticResolutionState = qs.getResource("?automaticResolutionState").toString();

            String querySelectPart = "SELECT ?s ?p ?o %s %s %n";
            String sparqlQueryRevisionA = null;
            String sparqlQueryRevisionB = null;

            // A
            if (currentTripleStateA.equals(MergeTripleStateEnum.ADDED.getSdRepresentation())) {
                // In revision A the triple was added
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, MergeTripleStateEnum.ADDED.getDgRepresentation());
            } else if (currentTripleStateA.equals(MergeTripleStateEnum.DELETED.getSdRepresentation())) {
                // In revision A the triple was deleted
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, MergeTripleStateEnum.DELETED.getDgRepresentation());
            } else if (currentTripleStateA.equals(MergeTripleStateEnum.ORIGINAL.getSdRepresentation())) {
                // In revision A the triple is original
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, MergeTripleStateEnum.ORIGINAL.getDgRepresentation());
            } else if (currentTripleStateA.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation())) {
                // In revision A the triple is not included
                querySelectPart = String.format(querySelectPart, "", "%s");
                sparqlQueryRevisionA = sparqlTemplateNotExistsRevisionA;
            }

            // B
            if (currentTripleStateB.equals(MergeTripleStateEnum.ADDED.getSdRepresentation())) {
                // In revision B the triple was added
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, MergeTripleStateEnum.ADDED.getDgRepresentation());
            } else if (currentTripleStateB.equals(MergeTripleStateEnum.DELETED.getSdRepresentation())) {
                // In revision B the triple was deleted
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, MergeTripleStateEnum.DELETED.getDgRepresentation());
            } else if (currentTripleStateB.equals(MergeTripleStateEnum.ORIGINAL.getSdRepresentation())) {
                // In revision B the triple is original
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, MergeTripleStateEnum.ORIGINAL.getDgRepresentation());
            } else if (currentTripleStateB.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation())) {
                // In revision B the triple is not included
                querySelectPart = String.format(querySelectPart, "");
                sparqlQueryRevisionB = sparqlTemplateNotExistsRevisionB;
            }

            // Concatenated SPARQL query
            String query = String.format(
                    Config.prefixes
                            + "%s"
                            + "WHERE { %n"
                            + "%s"
                            + "%s"
                            + "} %n", querySelectPart, sparqlQueryRevisionA, sparqlQueryRevisionB);

            // Iterate over all triples
            ResultSet resultSetTriples = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
            while (resultSetTriples.hasNext()) {
                QuerySolution qsQuery = resultSetTriples.next();

                String subject = qsQuery.getResource("?s").toString();
                String predicate = qsQuery.getResource("?p").toString();

                // Differ between literal and resource
                String object = "";
                if (qsQuery.get("?o").isLiteral()) {
                    object = "\"" + qsQuery.getLiteral("?o").toString() + "\"";
                } else {
                    object = "<" + qsQuery.getResource("?o").toString() + ">";
                }

                // Create the references A and B part of the query
                String referencesAB = ". %n";
                if (!currentTripleStateA.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation()) && !currentTripleStateB.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation())) {
                    referencesAB = String.format(
                            "			mmo:referencesA <%s> ; %n"
                                    + "			mmo:referencesB <%s> %n", qsQuery.getResource("?revisionA").toString(),
                            qsQuery.getResource("?revisionB").toString());
                } else if (currentTripleStateA.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation()) && !currentTripleStateB.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation())) {
                    referencesAB = String.format(
                            "			mmo:referencesB <%s> %n", qsQuery.getResource("?revisionB").toString());
                } else if (!currentTripleStateA.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation()) && currentTripleStateB.equals(MergeTripleStateEnum.NOTINCLUDED.getSdRepresentation())) {
                    referencesAB = String.format(
                            "			mmo:referencesA <%s> %n", qsQuery.getResource("?revisionA").toString());
                }

                String queryTriple = Config.prefixes + String.format(
                        "INSERT DATA { GRAPH <%s> {%n"
                                + "	<%s> a mmo:DifferenceGroup ; %n"
                                + "	mmo:hasTripleStateA <%s> ; %n"
                                + "	mmo:hasTripleStateB <%s> ; %n"
                                + "	mmo:isConflicting %s ; %n"
                                + "	mmo:automaticResolutionState <%s> ; %n"
                                + "	mmo:hasDifference [ %n"
                                + "		a mmo:Difference ; %n"
                                + "			mmo:hasTriple [ %n"
                                + "				rdf:subject <%s> ; %n"
                                + "				rdf:predicate <%s> ; %n"
                                + "				rdf:object %s %n"
                                + "			] ; %n"
                                + "%s"
                                + "	] . %n"
                                + "} }", graphNameDifferenceTripleModel,
                        currentDifferenceCombinationURI,
                        currentTripleStateA,
                        currentTripleStateB,
                        currentConflictState,
                        currentAutomaticResolutionState,
                        subject,
                        predicate,
                        object,
                        referencesAB);

                TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryTriple);
            }
        }
    }

}