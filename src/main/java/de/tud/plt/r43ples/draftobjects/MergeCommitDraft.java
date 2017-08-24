package de.tud.plt.r43ples.draftobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.*;
import de.tud.plt.r43ples.merging.SDDTripleStateEnum;
import de.tud.plt.r43ples.optimization.PathCalculationInterface;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new merge commit.
 *
 * @author Stephan Hensel
 */
public class MergeCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(MergeCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternMergeQuery = Pattern.compile(
            "(?<action>MERGE|REBASE|MERGE FF)\\s*(?<type>FORCE|AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameFrom>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameInto>[^\"]*?)\"(?<with>\\s*WITH\\s*\\{(?<triples>.*)\\})?",
            patternModifier); //TODO add COUNT for advanced rebase

    /** The triples of the query WITH part. **/
    private String triples;
    /** The branch name (from). **/
    private String branchNameFrom;
    /** The branch name (into). **/
    private String branchNameInto;
    /** The SDD URI to use. **/
    private String sdd;
    /** The graph name **/
    private String graphName;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;
    /** The query type (FORCE, AUTO, MANUAL). **/
    private MergeTypes type;
    /** The query action (MERGE, REBASE, MERGE FF). **/
    private MergeActions action;
    /** States if the WITH part is available. **/
    private boolean with;

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
    public MergeCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        // Dependencies
        this.pathCalculationInterface = PathCalculationSingleton.getInstance();

        this.extractRequestInformation();
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates an merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param sdd the SDD URI to use
     * @param action the query action (MERGE, REBASE, MERGE FF)
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    protected MergeCommitDraft(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String sdd, MergeActions action, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        super(null);
        // Dependencies
        this.pathCalculationInterface = PathCalculationSingleton.getInstance();

        this.setUser(user);
        this.setMessage(message);

        this.graphName = graphName;
        this.revisionGraph = new RevisionGraph(graphName);
        this.branchNameFrom = branchNameFrom;
        this.branchNameInto = branchNameInto;
        this.sdd = sdd;
        this.action = action;
        this.triples = triples;
        this.type = type;
        this.with = with;

        this.isCreatedWithRequest = false;
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        Matcher m = patternMergeQuery.matcher(getRequest().query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;

            switch (m.group("action").toUpperCase()) {
                case "MERGE":
                    action = MergeActions.MERGE;
                    break;
                case "REBASE":
                    action = MergeActions.REBASE;
                    break;
                case "MERGE FF":
                    action = MergeActions.MERGE_FF;
                    break;
                default:
                    action = null;
                    break;
            }
            String typeID = m.group("type");
            if (typeID != null) {
                switch (typeID.toUpperCase()) {
                    case "AUTO":
                        type = MergeTypes.AUTO;
                        break;
                    case "MANUAL":
                        type = MergeTypes.MANUAL;
                        break;
                    case "FORCE":
                        type = MergeTypes.FORCE;
                        break;
                    default:
                        type = null;
                        break;
                }
            } else {
                type = null;
            }

            graphName = m.group("graph");
            revisionGraph = new RevisionGraph(graphName);
            sdd = m.group("sdd");
            branchNameFrom = m.group("branchNameFrom").toLowerCase();
            branchNameInto = m.group("branchNameInto").toLowerCase();
            with = m.group("with") != null;
            triples = m.group("triples");

            logger.debug("type: " + type);
            logger.debug("graph: " + graphName);
            logger.debug("sdd: " + sdd);
            logger.debug("branchNameFrom: " + branchNameFrom);
            logger.debug("branchNameInto: " + branchNameInto);
            logger.debug("with: " + with);
            logger.debug("triples: " + triples);
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }

    }

    /**
     * Creates the commit draft as a new commit in the triple store and creates the corresponding revisions.
     *
     * @return the created commit
     */
    protected MergeCommit createCommitInTripleStore() throws InternalErrorException {
        // Select the right child element and create a corresponding commit using the createCommitInTripleStore method.
        if (action.equals(MergeActions.MERGE) && ((type == null) || !type.equals(MergeTypes.FORCE))) {
            ThreeWayMergeCommitDraft threeWayMergeCommit = new ThreeWayMergeCommitDraft(graphName, branchNameFrom, branchNameInto, getUser(), getMessage(), sdd, triples, type, with);
            return threeWayMergeCommit.createCommitInTripleStore();
        } else if (action.equals(MergeActions.MERGE_FF) && (type == null)) {
            FastForwardMergeCommitDraft fastForwardMergeCommitDraft = new FastForwardMergeCommitDraft(graphName, branchNameFrom, branchNameInto, getUser(), getMessage(), sdd, triples, type, with);
            return fastForwardMergeCommitDraft.createCommitInTripleStore();
        } else if (action.equals(MergeActions.REBASE)) {
            // TODO Rebase
            // TODO Advanced rebase
            throw new InternalErrorException("Rebase and advanced rebase currently not implemented.");
        } else {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }
    }

    /**
     * Get the triples of the query WITH part.
     *
     * @return the triples of the query WITH part
     */
    protected String getTriples() {
        return triples;
    }

    /**
     * Get the branch name (from).
     *
     * @return the branch name
     */
    protected String getBranchNameFrom() {
        return branchNameFrom;
    }

    /**
     * Get the branch name (into).
     *
     * @return the branch name
     */
    protected String getBranchNameInto() {
        return branchNameInto;
    }

    /**
     * Get the SDD URI to use.
     *
     * @return the SDD URI to use
     */
    protected String getSdd() {
        return sdd;
    }

    /**
     * The revision graph.
     *
     * @return the revision graph
     */
    protected RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * The graph name.
     *
     * @return the graph name
     */
    protected String getGraphName() {
        return graphName;
    }

    /**
     * Get the query type (FORCE, AUTO, MANUAL).
     *
     * @return the query type
     */
    protected MergeTypes getType() {
        return type;
    }

    /**
     * Get the query action (MERGE, REBASE, MERGE FF).
     *
     * @return the query action
     */
    protected MergeActions getAction() {
        return action;
    }

    /**
     * Get the boolean indicator if the WITH part os available.
     *
     * @return true if the WITH part is available
     */
    protected boolean isWith() {
        return with;
    }

    /**
     * Get the path calculation interface.
     *
     * @return the path calculation interface
     */
    protected PathCalculationInterface getPathCalculationInterface() {
        return pathCalculationInterface;
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
    protected void createRevisionProgresses(final String revisionGraph, final String graphName,
                                                Path pathFrom, String graphNameRevisionProgressFrom, String uriFrom,
                                                Path pathInto, String graphNameRevisionProgressInto, String uriInto, Revision commonRevision) throws InternalErrorException {
        logger.info("Create the revision progress of branch from and into.");

        RevisionGraph graph = new RevisionGraph(graphName);

        if (!((pathFrom.getRevisionPath().size() > 0) && (pathInto.getRevisionPath().size() > 0))) {
            throw new InternalErrorException("Revision path contains no revisions.");
        }

        // Get the full graph name of common revision or create full revision graph of common revision
        String fullGraphNameCommonRevision;
        Boolean tempGraphWasCreated = false;
        try {
            fullGraphNameCommonRevision = graph.getReferenceGraph(commonRevision.getRevisionIdentifier());
        } catch (InternalErrorException e) {
            // Create a temporary full graph
            // TODO move to new RevisionManagement
            fullGraphNameCommonRevision = graphName + "RM-TEMP-REVISION-PROGRESS-FULLGRAPH";
            RevisionManagementOriginal.generateFullGraphOfRevision(graphName, commonRevision.getRevisionIdentifier(), fullGraphNameCommonRevision);
            tempGraphWasCreated = true;
        }

        // Create revision progress of branch from
        createRevisionProgress(revisionGraph, pathFrom, fullGraphNameCommonRevision, graphNameRevisionProgressFrom, uriFrom);

        // Create revision progress of branch into
        createRevisionProgress(revisionGraph, pathInto, fullGraphNameCommonRevision, graphNameRevisionProgressInto, uriInto);

        // Drop the temporary full graph
        if (tempGraphWasCreated) {
            logger.info("Drop the temporary full graph.");
            TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + fullGraphNameCommonRevision + ">");
        }

    }

    /**
     * Create the revision progress.
     *
     * @param path the path with all revisions from start revision to target revision
     * @param fullGraphNameCommonRevision the full graph name of the common revision (first revision of path)
     * @param graphNameRevisionProgress the graph name of the revision progress
     * @param uri the URI of the revision progress
     * @throws InternalErrorException
     */
    protected void createRevisionProgress(final String revisionGraph, Path path, String fullGraphNameCommonRevision, String graphNameRevisionProgress, String uri) throws InternalErrorException {
        logger.info("Create the revision progress of " + uri + " in graph " + graphNameRevisionProgress + ".");

        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameRevisionProgress));
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE GRAPH  <%s>", graphNameRevisionProgress));
        Iterator<Revision> iteList = path.getRevisionPath().iterator();

        if (iteList.hasNext()) {
            String firstRevision = iteList.next().getRevisionURI();

            // Create the initial content
            logger.info("Create the initial content.");
            String queryInitial = Config.prefixes + String.format(
                    "INSERT { GRAPH <%s> { %n"
                            + "	<%s> a rpo:RevisionProgress; %n"
                            + "		rpo:original [ %n"
                            + "			rdf:subject ?s ; %n"
                            + "			rdf:predicate ?p ; %n"
                            + "			rdf:object ?o ; %n"
                            + "			rmo:references <%s> %n"
                            + "		] %n"
                            + "} } WHERE { %n"
                            + "	GRAPH <%s> %n"
                            + "		{ ?s ?p ?o . } %n"
                            + "}", graphNameRevisionProgress, uri, firstRevision, fullGraphNameCommonRevision);

            // Execute the query which generates the initial content
            TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryInitial);

            // Update content by current add and delete set - remove old entries
            while (iteList.hasNext()) {
                String revision = iteList.next().getRevisionURI();
                logger.info("Update content by current add and delete set of revision " + revision + " - remove old entries.");
                // Get the ADD and DELETE set URIs
                String addSetURI = RevisionManagementOriginal.getAddSetURI(revision, revisionGraph);
                String deleteSetURI = RevisionManagementOriginal.getDeleteSetURI(revision, revisionGraph);

                if ((addSetURI != null) && (deleteSetURI != null)) {

                    // Update the revision progress with the data of the current revision ADD set

                    // Delete old entries (original)
                    String queryRevision = Config.prefixes + String.format(
                            "DELETE { GRAPH <%s> { %n"
                                    + "	<%s> rpo:original ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:original ?blank . %n"
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
                                    + "	<%s> rpo:added ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:added ?blank . %n"
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

                    // Delete old entries (removed)
                    queryRevision += String.format(
                            "DELETE { GRAPH <%s> { %n"
                                    + "	<%s> rpo:removed ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:removed ?blank . %n"
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
                                    + "	<%s> a rpo:RevisionProgress; %n"
                                    + "		rpo:added [ %n"
                                    + "			rdf:subject ?s ; %n"
                                    + "			rdf:predicate ?p ; %n"
                                    + "			rdf:object ?o ; %n"
                                    + "			rmo:references <%s> %n"
                                    + "		] %n"
                                    + "} } WHERE { %n"
                                    + "	GRAPH <%s> %n"
                                    + "		{ ?s ?p ?o . } %n"
                                    + "};", graphNameRevisionProgress, uri, revision, addSetURI);

                    queryRevision += "\n \n";

                    // Update the revision progress with the data of the current revision DELETE set

                    // Delete old entries (original)
                    queryRevision += String.format(
                            "DELETE { GRAPH <%s> { %n"
                                    + "	<%s> rpo:original ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:original ?blank . %n"
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
                                    + "	<%s> rpo:added ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:added ?blank . %n"
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

                    // Delete old entries (removed)
                    queryRevision += String.format(
                            "DELETE { GRAPH <%s> { %n"
                                    + "	<%s> rpo:removed ?blank . %n"
                                    + "	?blank rdf:subject ?s . %n"
                                    + "	?blank rdf:predicate ?p . %n"
                                    + "	?blank rdf:object ?o . %n"
                                    + "	?blank rmo:references ?revision . %n"
                                    + "} } %n"
                                    + "WHERE { "
                                    + "		GRAPH <%s> { %n"
                                    + "			<%s> rpo:removed ?blank . %n"
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

                    // Insert new entries (removed)
                    queryRevision += String.format(
                            "INSERT { GRAPH <%s> { %n"
                                    + "	<%s> a rpo:RevisionProgress; %n"
                                    + "		rpo:removed [ %n"
                                    + "			rdf:subject ?s ; %n"
                                    + "			rdf:predicate ?p ; %n"
                                    + "			rdf:object ?o ; %n"
                                    + "			rmo:references <%s> %n"
                                    + "		] %n"
                                    + "} } WHERE { %n"
                                    + "	GRAPH <%s> %n"
                                    + "		{ ?s ?p ?o . } %n"
                                    + "}", graphNameRevisionProgress, uri, revision, deleteSetURI);

                    // Execute the query which updates the revision progress by the current revision
                    TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);

                } else {
                    //TODO Error management - is needed when a ADD or DELETE set is not referenced in the current implementation this error should not occur
                    logger.error("ADD or DELETE set of " + revision + "does not exists.");
                }
                logger.info("Revision progress was created.");
            }
        }
    }

    /**
     * Create the difference triple model which contains all differing triples.
     *
     * @param graphName the graph name
     * @param graphNameDifferenceTripleModel the graph name of the difference triple model
     * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
     * @param uriA the URI of the revision progress of branch A
     * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
     * @param uriB the URI of the revision progress of branch B
     * @param uriSDD the URI of the SDD to use
     */
    protected void createDifferenceTripleModel(String graphName, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD){

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
                "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
                        + "PREFIX sdd:  <http://eatld.et.tu-dresden.de/sdd#> %n"
                        + "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> %n"
                        + "SELECT ?combinationURI ?tripleStateA ?tripleStateB ?conflict ?automaticResolutionState %n"
                        + "WHERE { GRAPH <%s> { %n"
                        + "	<%s> a sddo:StructuralDefinitionGroup ;"
                        + "		sddo:hasStructuralDefinition ?combinationURI ."
                        + "	?combinationURI a sddo:StructuralDefinition ; %n"
                        + "		sddo:hasTripleStateA ?tripleStateA ; %n"
                        + "		sddo:hasTripleStateB ?tripleStateB ; %n"
                        + "		sddo:isConflicting ?conflict ; %n"
                        + "		sddo:automaticResolutionState ?automaticResolutionState . %n"
                        + "} } %n", Config.sdd_graph, uriSDD);

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
            if (currentTripleStateA.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
                // In revision A the triple was added
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.ADDED.getRpoRepresentation());
            } else if (currentTripleStateA.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
                // In revision A the triple was deleted
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.DELETED.getRpoRepresentation());
            } else if (currentTripleStateA.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
                // In revision A the triple is original
                querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
                sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.ORIGINAL.getRpoRepresentation());
            } else if (currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
                // In revision A the triple is not included
                querySelectPart = String.format(querySelectPart, "", "%s");
                sparqlQueryRevisionA = sparqlTemplateNotExistsRevisionA;
            }

            // B
            if (currentTripleStateB.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
                // In revision B the triple was added
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.ADDED.getRpoRepresentation());
            } else if (currentTripleStateB.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
                // In revision B the triple was deleted
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.DELETED.getRpoRepresentation());
            } else if (currentTripleStateB.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
                // In revision B the triple is original
                querySelectPart = String.format(querySelectPart, "?revisionB");
                sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.ORIGINAL.getRpoRepresentation());
            } else if (currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
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
                if (!currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
                    referencesAB = String.format(
                            "			rpo:referencesA <%s> ; %n"
                                    + "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionA").toString(),
                            qsQuery.getResource("?revisionB").toString());
                } else if (currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
                    referencesAB = String.format(
                            "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionB").toString());
                } else if (!currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
                    referencesAB = String.format(
                            "			rpo:referencesA <%s> %n", qsQuery.getResource("?revisionA").toString());
                }

                String queryTriple = Config.prefixes + String.format(
                        "INSERT DATA { GRAPH <%s> {%n"
                                + "	<%s> a rpo:DifferenceGroup ; %n"
                                + "	sddo:hasTripleStateA <%s> ; %n"
                                + "	sddo:hasTripleStateB <%s> ; %n"
                                + "	sddo:isConflicting %s ; %n"
                                + "	sddo:automaticResolutionState <%s> ; %n"
                                + "	rpo:hasDifference [ %n"
                                + "		a rpo:Difference ; %n"
                                + "			rpo:hasTriple [ %n"
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