package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Endpoint;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Coevolve all dependent graphs based upon aggregated atomic changes. Uses the coevolution rules provided which are associated with the HLC aggregation.
 * Example query: COEVO GRAPH <http://test.com/r43ples-dataset-hlc-aggregation> REVISION "1" TO REVISION "2"
 * Currently the implementation only works if the end revision is the direct succeeding revision of the start revision.
 * Dependent graphs currently only identified on their master branch. The result of the coevolution is currently always committed to the master branch.
 *
 * @author Stephan Hensel
 */
public class CoEvolutionDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SelectConstructAskQuery.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternAggQuery = Pattern.compile(
            "COEVO\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*REVISION\\s*\"(?<startRevisionIdentifier>[^\"]*?)\"\\s*TO\\s*REVISION\\s*\"(?<endRevisionIdentifier>[^\"]*?)\"\\s*",
            patternModifier);

    /** The corresponding R43ples request. **/
    private R43plesRequest request;
    /** The start revision identifier. **/
    private String startRevisionIdentifier;
    /** The end revision identifier. **/
    private String endRevisionIdentifier;
    /** The change set between start and end revision. **/
    private ChangeSet changeSetStartToEnd;
    /** The graph name **/
    private String graphName;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;

    // Dependencies
    /** The triplestore interface to use. **/
    private TripleStoreInterface tripleStoreInterface;
    /** The current URI calculator instance. */
    private URICalculator uriCalculator;
    /** The endpoint. **/
    private Endpoint ep = new Endpoint();


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @return the query response
     * @throws InternalErrorException
     */
    protected CoEvolutionDraft(R43plesRequest request) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        this.uriCalculator = new URICalculator();

        this.request = request;

        this.extractRequestInformation();
    }

    /**
     * Triggers the coevolution process and writes meta information into revision graph and the coevolution graph.
     *
     * @return the evolution object containing all performed coevolutions
     * @throws InternalErrorException
     */
    protected Evolution coevolveAll() throws InternalErrorException {

        // Stores all meta information of the evolution, has to be integrated into a SPARQL UPDATE query if all data is collected
        StringBuilder metaInformationN3 = new StringBuilder();
        String evolutionURI = uriCalculator.getRandomURI(Config.evolution_graph);

        metaInformationN3.append(String.format(
                "<%1$s> a rmo:Evolution. %n" +
                "<%1$s> rmo:startRevision <%2$s>. %n" +
                "<%1$s> rmo:endRevision <%3$s>. %n" +
                "<%1$s> rmo:usedSourceRevisionGraph <%4$s>. %n",
                evolutionURI, revisionGraph.getRevision(startRevisionIdentifier).getRevisionURI(),
                revisionGraph.getRevision(endRevisionIdentifier).getRevisionURI(), revisionGraph.getGraphName()));

        // Get associated semantic changes with specified change set
        LinkedList<SemanticChange> semanticChanges = changeSetStartToEnd.getSemanticChangesList();

        // Check if semantic changes are not generated
        if ((semanticChanges == null) || (semanticChanges.isEmpty())) {
            // Aggregate
            AggregationDraft aggregationDraft = new AggregationDraft(revisionGraph, changeSetStartToEnd);
            semanticChanges = aggregationDraft.aggregate();
            if ((semanticChanges == null) || (semanticChanges.isEmpty())) {
                throw new InternalErrorException("There are no semantic changes between revision " + startRevisionIdentifier + " and revision " + endRevisionIdentifier + "as basis for the coevolution.");
            }
        }

        // Coevolution rule list
        LinkedList<CoEvoRule> coevolutionRules = new LinkedList<>();

        // Iterate through all available semantic changes and store results within the coevolution rule list
        for (SemanticChange semanticChange : semanticChanges) {
            // Get the rule of the semantic change and check if a coevolution part is specified
            CoEvoRule coEvoRule = new CoEvoRule(semanticChange);
            coevolutionRules.add(coEvoRule);
            metaInformationN3.append(String.format("<%s> rmo:associatedSemanticChange <%s>. %n", evolutionURI, semanticChange.getSemanticChangeURI()));
        }

        // Get all graphs within the repository
        HashMap<String, RevisionGraph> revisedGraphs = new RevisionControl().getRevisedGraphs();
        // Remove the graph which should be coevolved
        revisedGraphs.remove(this.revisionGraph.getGraphName());

        // Iterate through the revised graphs and search for dependencies (Check the master branch of each revised graph if there is a dependency)
        for (String graphName : revisedGraphs.keySet()) {
            RevisionGraph revisionGraph = revisedGraphs.get(graphName);

            // URI of the coevolution of the current revision graph
            String coevolutionURI = uriCalculator.getRandomURI(Config.evolution_graph);
            metaInformationN3.append(String.format(
                    "<%1$s> rmo:performedCoEvolution <%2$s>. %n" +
                    "<%2$s> a rmo:CoEvolution. %n" +
                    "<%2$s> rmo:usedTargetRevisionGraph <%3$s>. %n" +
                    "<%2$s> rmo:usedTargetBranch <%4$s>. %n",
                    evolutionURI, coevolutionURI, revisionGraph.getGraphName(), revisionGraph.getBranchUri("master")));

            // Create temporary named graphs for add and delete
            String tempAddSetURI = uriCalculator.getRandomNamedGraphURI(graphName);
            String tempDeleteSetURI = uriCalculator.getRandomNamedGraphURI(graphName);

            tripleStoreInterface.executeCreateGraph(tempAddSetURI);
            tripleStoreInterface.executeCreateGraph(tempDeleteSetURI);

            // Iterate through all available semantic changes and apply the corresponding coevolution match part of the rule
            for (CoEvoRule coEvoRule : coevolutionRules) {

                String matchQuery = coEvoRule.getDependencyMatchingQuery().replaceAll("<http://NAMEDGRAPH#master>", "<" + graphName + ">");
                ResultSet resultSetMatchings = tripleStoreInterface.executeSelectQuery(matchQuery);

                // If the result set is not equal to null a coevolution can be executed
                // URI of the coevolution of the current revision graph
                String appliedCoevolutionURI = uriCalculator.getRandomURI(Config.evolution_graph);
                metaInformationN3.append(String.format(
                        "<%1$s> aero:appliedCoEvolutionRule <%2$s>. %n" +
                        "<%2$s> a aero:AppliedCoEvolutionRule. %n" +
                        "<%2$s> aero:usedRule <%3$s>. %n" +
                        "<%2$s> aero:usedSemanticChange <%4$s>. %n",
                        coevolutionURI, appliedCoevolutionURI, coEvoRule.getSemanticChange().getUsedRuleURI(), coEvoRule.getSemanticChange().getSemanticChangeURI()));

                // Maybe there are multiple matches within one graph
                while (resultSetMatchings.hasNext()) {
                    QuerySolution qsMatching = resultSetMatchings.next();

                    String sparqlVariableGroupURI = uriCalculator.getRandomURI(Config.evolution_graph);
                    metaInformationN3.append(String.format(
                            "<%1$s> aero:hasVariableGroup <%2$s>. %n" +
                            "<%2$s> a aero:SPARQLVariableGroup. %n",
                            appliedCoevolutionURI, sparqlVariableGroupURI));

                    // Create a list of SPARQL variables and the results regarding the current revision graph
                    LinkedList<SparqlVariable> sparqlVariablesListCurrentRevisionGraph = new LinkedList<>();

                    String addSetInsertQuery = coEvoRule.getAddSetInsertQuery();
                    addSetInsertQuery = addSetInsertQuery.replaceAll("<http://NAMEDGRAPH#master>", "<" + graphName + ">");
                    addSetInsertQuery = addSetInsertQuery.replaceAll("<http://NAMEDGRAPH#ADDSET-NEW>", "<" + tempAddSetURI + ">");
                    String deleteSetInsertQuery = coEvoRule.getDeleteSetInsertQuery();
                    deleteSetInsertQuery = deleteSetInsertQuery.replaceAll("<http://NAMEDGRAPH#master>", "<" + graphName + ">");
                    deleteSetInsertQuery = deleteSetInsertQuery.replaceAll("<http://NAMEDGRAPH#DELETESET-NEW>", "<" + tempDeleteSetURI + ">");

                    for (SparqlVariable genericSparqlVariable : coEvoRule.getSparqlVariablesList()) {
                        String value;
                        String valueMetaInfo;
                        boolean isResource;
                        try {
                            value = qsMatching.getResource("?" + genericSparqlVariable.getVariableName()).toString();
                            valueMetaInfo = "<" + value + ">";
                            isResource = true;
                        } catch (Exception e) {
                            value = qsMatching.getLiteral("?" + genericSparqlVariable.getVariableName()).toString();
                            valueMetaInfo = "\"" + value + "\"";
                            isResource = false;
                        }
                        SparqlVariable specificSparqlVariable = new SparqlVariable(revisionGraph, null, genericSparqlVariable.getVariableName(), genericSparqlVariable.getSpinResourceURI(), value, isResource);
                        sparqlVariablesListCurrentRevisionGraph.add(specificSparqlVariable);

                        addSetInsertQuery = addSetInsertQuery.replaceAll("\\?" + genericSparqlVariable.getVariableName(), valueMetaInfo);
                        deleteSetInsertQuery = deleteSetInsertQuery.replaceAll("\\?" + genericSparqlVariable.getVariableName(), valueMetaInfo);

                        tripleStoreInterface.executeUpdateQuery(addSetInsertQuery);
                        tripleStoreInterface.executeUpdateQuery(deleteSetInsertQuery);

                        String sparqlVariableURI = uriCalculator.getRandomURI(Config.evolution_graph);
                        metaInformationN3.append(String.format(
                                "<%1$s> aero:hasVariables <%2$s>. %n" +
                                "<%2$s> a aero:SPARQLVariable. %n" +
                                "<%2$s> sp:varName \"%3$s\". %n" +
                                "<%2$s> aero:value %4$s. %n" +
                                "<%2$s> aero:spinResource <%5$s>. %n",
                                sparqlVariableGroupURI, sparqlVariableURI, genericSparqlVariable.getVariableName(), valueMetaInfo, genericSparqlVariable.getSpinResourceURI()));

                    }
                }

            }

            String addSetN3 = Helper.getContentOfNamedGraphAsN3(tempAddSetURI);
            String deleteSetN3 = Helper.getContentOfNamedGraphAsN3(tempDeleteSetURI);

            // Create a new update commit with the specified add and delete sets from temporary graphs for the current revision graph
            Branch branch = new Branch(revisionGraph, "master", true);
            // TODO user and commit message
            UpdateCommitDraft updateCommitDraft = new UpdateCommitDraft(graphName, addSetN3, deleteSetN3, "TEST", "TEST", branch);
            UpdateCommit updateCommit = updateCommitDraft.createInTripleStore().get(0);

            tripleStoreInterface.executeUpdateQuery("DROP SILENT GRAPH <" + tempAddSetURI + ">");
            tripleStoreInterface.executeUpdateQuery("DROP SILENT GRAPH <" + tempDeleteSetURI + ">");

            // Create the generated revision
            metaInformationN3.append(String.format(
                    "<%1$s> rmo:generated <%2$s>. %n",
                    coevolutionURI, updateCommit.getGeneratedRevision().getRevisionURI()));
        }

        // Write meta data into evolution graph
        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", Config.evolution_graph, metaInformationN3.toString());
        tripleStoreInterface.executeUpdateQuery(queryRevision);

        //TODO Extend the rule set and the semantic description of it within rules.ttl and AERO

        return new Evolution(evolutionURI);
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        Matcher m = patternAggQuery.matcher(this.request.query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;

            graphName = m.group("graph");
            revisionGraph = new RevisionGraph(graphName);

            startRevisionIdentifier = m.group("startRevisionIdentifier");
            endRevisionIdentifier = m.group("endRevisionIdentifier");

            logger.debug("graph: " + graphName);
            logger.debug("startRevisionIdentifier: " + startRevisionIdentifier);
            logger.debug("endRevisionIdentifier: " + endRevisionIdentifier);
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + this.request.query_sparql);
        }

        ArrayList<ChangeSet> changeSets = revisionGraph.getRevision(endRevisionIdentifier).getChangeSets();
        for (ChangeSet changeSet : changeSets) {
            if (changeSet.getPriorRevision().getRevisionIdentifier().equals(startRevisionIdentifier)) {
                changeSetStartToEnd = changeSet;
            }
        }

    }

}
