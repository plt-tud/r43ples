package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.HighLevelChanges;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Endpoint;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Aggregation of atomic changes to high level ones based upon the provided aggregation rules as SPIN.
 * Example query: AGG GRAPH <http://test.com/r43ples-dataset-hlc-aggregation> REVISION "1" TO REVISION "2"
 * Currently the implementation only works if the end revision is the direct succeeding revision of the start revision.
 *
 * @author Stephan Hensel
 */
public class Aggregation {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SelectConstructAskQuery.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternAggQuery = Pattern.compile(
            "AGG\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*REVISION\\s*\"(?<startRevisionIdentifier>[^\"]*?)\"\\s*TO\\s*REVISION\\s*\"(?<endRevisionIdentifier>[^\"]*?)\"\\s*",
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
    protected Aggregation(R43plesRequest request) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        this.uriCalculator = new URICalculator();

        this.request = request;

        this.extractRequestInformation();
    }

    /**
     * Triggers the aggregation process and writes meta information into revision graph.
     *
     * @throws InternalErrorException
     */
    protected HighLevelChanges aggregate() throws InternalErrorException {

        // Get all available aggregation rules
        String queryAggRules = String.format(
                Config.prefixes
                        + "SELECT ?aggrule %n"
                        + "WHERE { GRAPH <%s> { %n"
                        + "	?aggrule a aero:HLCAggRule . %n"
                        + "} } %n", Config.rules_graph);

        // Iterate over available aggregation rules
        ResultSet resultSetAggRules = tripleStoreInterface.executeSelectQuery(queryAggRules);
        while (resultSetAggRules.hasNext()) {
            QuerySolution qs = resultSetAggRules.next();

            String aggRuleURI = qs.getResource("?aggrule").toString();

            // Get the SPIN query
            String querySpinQuery = String.format(
                    Config.prefixes
                            + "SELECT ?query %n"
                            + "WHERE { GRAPH <%s> { %n"
                            + "	<%s> a aero:HLCAggRule ; %n"
                            + "      aero:spinQuery ?query ."
                            + "} } %n", Config.rules_graph, aggRuleURI);
            ResultSet resultSetSpinRule = tripleStoreInterface.executeSelectQuery(querySpinQuery);
            String spinURI = null;
            while (resultSetSpinRule.hasNext()) {
                QuerySolution qsSpin = resultSetSpinRule.next();
                spinURI = qsSpin.getResource("?query").toString();
            }
            String spinQueryN3 = Helper.getAllRelatedElementsToURI(Config.rules_graph, spinURI);

            // Create the SPARQL query
            String sparqlAggQuery = Helper.getSparqlQueryFromSpin(spinQueryN3, spinURI);

            // Replace placeholder with current request information
            sparqlAggQuery = sparqlAggQuery.replace("<http://NAMEDGRAPH#ADDSET-1-2>", "<" + changeSetStartToEnd.getAddSetURI() + ">");
            sparqlAggQuery = sparqlAggQuery.replace("<http://NAMEDGRAPH#DELETESET-1-2>", "<" + changeSetStartToEnd.getDeleteSetURI() + ">");
            sparqlAggQuery = sparqlAggQuery.replace("<http://NAMEDGRAPH#rev1>", "<" + graphName + "> REVISION \"" + startRevisionIdentifier + "\"");
            sparqlAggQuery = sparqlAggQuery.replace("<http://NAMEDGRAPH#rev2>", "<" + graphName + "> REVISION \"" + endRevisionIdentifier + "\"");

            String sparqlResult = ep.sparql(sparqlAggQuery).getEntity().toString();

            ResultSet resultSet = ResultSetFactory.fromXML(new ByteArrayInputStream(sparqlResult.getBytes()));

            while (resultSet.hasNext()) {
                QuerySolution qsResult = resultSet.next();
                addMetaInformation(aggRuleURI, spinQueryN3, spinURI, qsResult);
            }

        }

        return null;
    }

    /**
     * Adds meta information of aggregation to the revision graph.
     *
     * @param aggRuleURI the aggregation rule URI
     * @param spinQueryN3 the SPIN query as N3
     * @param spinURI the URI of the SPIN query
     * @param qsResult the query result after execution of the SPIN query
     */
    private void addMetaInformation(String aggRuleURI, String spinQueryN3, String spinURI, QuerySolution qsResult) {
        // Basic meta data
        String semanticChangeURI = uriCalculator.getRandomURI(revisionGraph);
        String additionsURI = uriCalculator.getRandomURI(revisionGraph);
        String deletionsURI = uriCalculator.getRandomURI(revisionGraph);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "<%s> a rmo:SemanticChange ; %n"
                        + "	rmo:additions <%s> ; %n"
                        + "	rmo:deletions <%s> ; %n"
                        + "	aero:usedRule <%s> . %n",
                semanticChangeURI, additionsURI, deletionsURI, aggRuleURI));

        // Variable meta data
        HashMap<String,String> variableMap = Helper.getVariableMapFromSpin(spinQueryN3, spinURI);
        HashMap<String,String> variableResultMap = new HashMap<>();

        Iterator ite = qsResult.varNames();
        while (ite.hasNext()) {
            String sparqlVariableURI = uriCalculator.getRandomURI(revisionGraph);
            String varName = ite.next().toString();
            String value;
            try {
                value = "<" + qsResult.getResource("?" + varName).toString() + ">";
            } catch (Exception e) {
                value = "\"" + qsResult.getLiteral("?" + varName).toString() + "\"";
            }
            variableResultMap.put(varName,value);
            sb.append(String.format(
                "<%s> aero:hasVariables <%s> . %n" +
                "<%s> a aero:SPARQLVariable ; %n"
                        + "	sp:varName \"%s\" ; %n"
                        + "	aero:value %s ; %n"
                        + "	aero:spinResource <%s> . %n",
                semanticChangeURI, sparqlVariableURI, sparqlVariableURI, varName, value, variableMap.get(varName)));

        }

        // TRIG data - Get additions and deletions and store them as TRIG

        // Get the additions and deletions SPIN queries
        String queryAddDelSpinQueries = String.format(
                Config.prefixes
                        + "SELECT ?queryAdd ?queryDel %n"
                        + "WHERE { GRAPH <%s> { %n"
                        + "	<%s> a aero:HLCAggRule ; %n"
                        + "      aero:addSetDetectionQuery ?subQueryAdd ; %n"
                        + "      aero:deleteSetDetectionQuery ?subQueryDel . %n"
                        + " ?subQueryAdd <http://spinrdf.org/sp#query> ?queryAdd. %n"
                        + " ?subQueryDel <http://spinrdf.org/sp#query> ?queryDel. %n"
                        + "} } %n", Config.rules_graph, aggRuleURI);
        ResultSet resultSetAddDelQueries = tripleStoreInterface.executeSelectQuery(queryAddDelSpinQueries);
        String spinAddURI = null;
        String spinDelURI = null;
        while (resultSetAddDelQueries.hasNext()) {
            QuerySolution qsSpin = resultSetAddDelQueries.next();
            spinAddURI = qsSpin.getResource("?queryAdd").toString();
            spinDelURI = qsSpin.getResource("?queryDel").toString();
        }

        String sparqlAddQuery = Helper.getSparqlQueryFromSpin(spinQueryN3, spinAddURI);
        String sparqlDelQuery = Helper.getSparqlQueryFromSpin(spinQueryN3, spinDelURI);

        // Get the WHERE part of the queries and replace the variables with query results to get the involved triples
        Pattern patternWherePart = Pattern.compile(
                "(?s:.)*WHERE\\s*\\{\\s*GRAPH(?s:.)*\\{(?<where>(?s:.)*)?\\}\\s*\\}",
                patternModifier);

        assert sparqlAddQuery != null;
        Matcher mAdd = patternWherePart.matcher(sparqlAddQuery);
        assert sparqlDelQuery != null;
        Matcher mDel = patternWherePart.matcher(sparqlDelQuery);

        mAdd.find();
        mDel.find();

        String triplesAdd = mAdd.group("where").trim();
        String triplesDel = mDel.group("where").trim();

        if (!triplesAdd.endsWith("."))
            triplesAdd = triplesAdd.concat(".");
        if (!triplesDel.endsWith("."))
            triplesDel = triplesDel.concat(".");

        for (String currentKey : variableResultMap.keySet()) {
            triplesAdd = triplesAdd.replaceAll("\\?" + currentKey, variableResultMap.get(currentKey));
            triplesDel = triplesDel.replaceAll("\\?" + currentKey, variableResultMap.get(currentKey));
        }

        Model modelAdd = JenaModelManagement.readTurtleStringToJenaModel(triplesAdd);

        sb.append(String.format("<%s> a rmo:Set . %n", additionsURI));

        StmtIterator iteStmtAdd = modelAdd.listStatements();
        while (iteStmtAdd.hasNext()) {
            Statement statement = iteStmtAdd.next();
            String statementURI = uriCalculator.getRandomURI(revisionGraph);
            sb.append(String.format(
                "<%s> rmo:statements <%s> . %n" +
                "<%s> a rdf:Statement ; %n"
                        + " rdf:subject <%s> ; %n"
                        + " rdf:predicate <%s> ; %n"
                        + " rdf:object <%s> . %n",
                additionsURI, statementURI, statementURI, statement.getSubject().toString(), statement.getPredicate(), statement.getObject()));
        }

        Model modelDel = JenaModelManagement.readTurtleStringToJenaModel(triplesDel);

        sb.append(String.format("<%s> a rmo:Set . %n", deletionsURI));

        StmtIterator iteStmtDel = modelDel.listStatements();
        while (iteStmtDel.hasNext()) {
            Statement statement = iteStmtDel.next();
            String statementURI = uriCalculator.getRandomURI(revisionGraph);
            sb.append(String.format(
                    "<%s> rmo:statements <%s> . %n" +
                            "<%s> a rdf:Statement ; %n"
                            + " rdf:subject <%s> ; %n"
                            + " rdf:predicate <%s> ; %n"
                            + " rdf:object <%s> . %n",
                    deletionsURI, statementURI, statementURI, statement.getSubject().toString(), statement.getPredicate(), statement.getObject()));
        }

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisionGraph.getRevisionGraphUri(), sb.toString());

        tripleStoreInterface.executeUpdateQuery(queryRevision);

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
