package de.tud.plt.r43ples.optimization;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class implements the PathCalculationInterface interface for a specific RevisionGraph and provides simple not optimized algorithms to calculate paths between revisions.
 *
 * @author Stephan Hensel
 *
 */
public class SimplePathCalculation implements PathCalculationInterface {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SimplePathCalculation.class);

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;

    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     */
    protected SimplePathCalculation(RevisionGraph revisionGraph) {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        this.revisionGraph = revisionGraph;
    }


    /**
     * Get the path to the nearest revision which has a full graph.
     *
     * @param revision revision where the search should start
     * @return path containing all revisions from start revision to next revision with a full graph
     * @throws InternalErrorException
     */
    @Override
    public Path getPathToRevisionWithFullGraph(Revision revision) throws InternalErrorException {
        logger.info("Get path to full graph revision starting from revision " + revision.getRevisionIdentifier());
        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision "
                + "WHERE { "
                + "    GRAPH <%1$s> {"
                + "        ?revision rmo:wasDerivedFrom* <%2$s> ."
                + "        ?reference rmo:references ?revision ."
                + "    }"
                + "}", revisionGraph.getRevisionGraphUri(), revision.getRevisionURI());
        this.logger.info(query);

        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);


        Path bestPath = null;

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String revision_uri_end = qs.getResource("?revision").toString();
            Revision revision_end = new Revision(revisionGraph, revision_uri_end, false);

            Path path = this.getPathBetweenStartAndTargetRevision(revision, revision_end);
            if (bestPath==null || bestPath.getRevisionPath().size()> path.getRevisionPath().size()){
            	this.logger.info("new best path: "+ path.getRevisionPath().size());
                bestPath = path;
            }
        }
        return bestPath;
    }


    /**
     * Get the path to the nearest revision which has a full graph.
     *
     * @param revision      revision where the search should start
     * @return path containing all revisions from start revision to next revision with a full graph
     * @throws InternalErrorException
     */
    @Override
    public ChangeSetPath getPathOfChangeSets(Revision revision) throws InternalErrorException {
        logger.info("Get path of change sets to full graph revision starting from revision " + revision.getRevisionIdentifier());

        Branch branch = revision.getAssociatedBranch();
        if (branch != null) {
            return new ChangeSetPath(revisionGraph, revision, revision);
        }

        // get nearest reference
        String query_select_nearest_reference = String.format("" +
                        "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n" +
                        "SELECT DISTINCT  ?branch (COUNT(?revision) as ?length)\n" +
                        "WHERE { \n" +
                        "  GRAPH <%s> { \n" +
                        "    ?lastRevision rmo:wasDerivedFrom* ?revision.\n" +
                        "    ?revision rmo:wasDerivedFrom* <%s>.\n" +
                        "    ?branch rmo:references ?lastRevision.\n" +
                        "  }\n" +
                        "}\n" +
                        "GROUP BY ?branch \n" +
                        "ORDER BY ?length\n" +
                        "LIMIT 1",
                revisionGraph.getRevisionGraphUri(), revision.getRevisionURI());
        this.logger.info(query_select_nearest_reference);

        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query_select_nearest_reference);

        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String branch_uri = qs.getResource("?branch").toString();
            branch = revisionGraph.getBranch(branch_uri, false);
            Revision revision_end = branch.getLeafRevision();

            return this.getChangeSetsBetweenStartAndTargetRevision(revision, revision_end);
        } else {
            throw new InternalErrorException("Could not find nearest branch for revision");
        }

    }


    /**
     * Get the common revision of the specified revisions which has the shortest path to the two.
     * To ensure wise results the revisions should be terminal branch nodes.
     *
     * @param revision1 the first revision should be a terminal branch node
     * @param revision2 the second revision should be a terminal branch node
     * @return the nearest common revision
     * @throws InternalErrorException
     */
    @Override
    public Revision getCommonRevisionWithShortestPath(Revision revision1, Revision revision2) throws InternalErrorException {
        logger.info("Get the common revision of revision " + revision1.getRevisionIdentifier() + " and revision " + revision2.getRevisionIdentifier() + " which has the shortest path.");
        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision "
                + "WHERE { "
                + "    GRAPH <%s> {"
                + "        <%2$s> rmo:wasDerivedFrom+ ?revision ."
                + "        <%3$s> rmo:wasDerivedFrom+ ?revision ."
                + "        ?next rmo:wasDerivedFrom ?revision."
                + "        FILTER NOT EXISTS {"
                + "            <%2$s> rmo:wasDerivedFrom+ ?next ."
                + "            <%3$s> rmo:wasDerivedFrom+ ?next ."
                + "        }"
                + "    }"
                + "}"
                + "LIMIT 1", revisionGraph.getRevisionGraphUri(), revision1.getRevisionURI(), revision2.getRevisionURI());
        this.logger.debug(query);

        ResultSet results = tripleStoreInterface.executeSelectQuery(query);

        if (results.hasNext()) {
            QuerySolution qs = results.next();
            logger.info("Common revision found.");
            return new Revision(revisionGraph, qs.getResource("?revision").toString(), false);
        } else {
            throw new InternalErrorException("No common revision of revision " + revision1.getRevisionIdentifier() + " and revision " + revision2.getRevisionIdentifier() + " could be found.");
        }
    }

    /**
     * Calculate the path from start revision to target revision.
     * Example: target rmo:wasDerivedFrom source
     *
     * @param startRevision the start revision
     * @param targetRevision the target revision
     * @return path containing all revisions from start revision to target revision
     */
    @Override
    public Path getPathBetweenStartAndTargetRevision(Revision startRevision, Revision targetRevision) throws InternalErrorException {
        logger.info("Calculate the shortest path from revision " + startRevision.getRevisionIdentifier() + " to " + targetRevision.getRevisionIdentifier() + ".");

        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision ?previousRevision %n"
                + "WHERE { %n"
                + "	GRAPH <%s> { %n"
                + "		<%s> rmo:wasDerivedFrom* ?revision."
                + "		?revision rmo:wasDerivedFrom* <%s>."
                + "		OPTIONAL{?revision rmo:wasDerivedFrom ?previousRevision}"
                + " }"
                + "}", revisionGraph.getRevisionGraphUri(), targetRevision.getRevisionURI(), startRevision.getRevisionURI());
        this.logger.debug(query);

        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        Path path = new Path(revisionGraph, startRevision, targetRevision);

        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);

        // Path element counter
        int counterLength = 0;

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String resource = qs.getResource("?revision").toString();
            String previousResource = null;
            if (qs.getResource("?previousRevision") != null) {
                previousResource = qs.getResource("?previousRevision").toString();
            }
            if (resultMap.containsKey(resource)) {
                resultMap.get(resource).add(previousResource);
            } else {
                ArrayList<String> arrayList = new ArrayList<String>();
                counterLength++;
                arrayList.add(previousResource);
                resultMap.put(resource, arrayList);
            }
        }

        // Sort the result map -> sorted list of path elements
        // A merged revision can have two predecessors -> it is important to choose the right predecessor revision according to the selected path
        String currentPathElement = targetRevision.getRevisionURI();
        for (int i = 0; i < counterLength; i++) {
            path.addRevisionToPathStart(new Revision(revisionGraph, currentPathElement, false));

            // Check if start revision was already reached
            if (currentPathElement.equals(startRevision.getRevisionURI())) {
                return path;
            }

            if (resultMap.get(currentPathElement).size() > 1) {
                if (resultMap.containsKey(resultMap.get(currentPathElement).get(0))) {
                    currentPathElement = resultMap.get(currentPathElement).get(0);
                } else {
                    currentPathElement = resultMap.get(currentPathElement).get(1);
                }
            } else {
                currentPathElement = resultMap.get(currentPathElement).get(0);
            }
        }

        return path;
    }


    /**
     * Calculate the path from start revision to target revision.
     * Example: target rmo:wasDerivedFrom source
     *
     * @param startRevision  the start revision
     * @param targetRevision the target revision
     * @return path containing all revisions from start revision to target revision
     */
    @Override
    public ChangeSetPath getChangeSetsBetweenStartAndTargetRevision(Revision startRevision, Revision targetRevision) throws InternalErrorException {
        logger.info("Calculate the shortest path from revision " + startRevision.getRevisionIdentifier() + " to " + targetRevision.getRevisionIdentifier() + ".");

        String query = String.format("" +
                        "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> %n" +
                        "SELECT DISTINCT ?revision ?nextRevision ?changeSet ?addSet ?deleteSet %n" +
                        "WHERE { %n" +
                        "	GRAPH <%s> { %n" +
                        "		<%s> rmo:wasDerivedFrom* ?revision, ?nextRevision." +
                        "		?revision rmo:wasDerivedFrom* <%s>. " +
                        "       ?nextRevision rmo:wasDerivedFrom ?revision. " +
                        "       ?changeSet rmo:succeedingRevision ?nextRevision. " +
                        "       ?changeSet rmo:priorRevision ?revision; " +
                        "                  rmo:addSet    ?addSet ; " +
                        "                  rmo:deleteSet ?deleteSet . " +
                        " }" +
                        "}",
                revisionGraph.getRevisionGraphUri(), targetRevision.getRevisionURI(), startRevision.getRevisionURI());
        this.logger.debug(query);

        ChangeSetPath path = new ChangeSetPath(revisionGraph, startRevision, targetRevision);

        ResultSetRewindable resultSet = ResultSetFactory.copyResults(tripleStoreInterface.executeSelectQuery(query));

        String currentRevisionUri = startRevision.getRevisionURI();
        String nextResource;
        String changeSet;

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String resource = qs.getResource("?revision").toString();
            if (resource.equals(currentRevisionUri)) {
                nextResource = qs.getResource("?nextRevision").toString();
                changeSet = qs.getResource("?changeSet").toString();

                Revision nextRevision = new Revision(revisionGraph, nextResource, false);
                String addSet = qs.getResource("?addSet").toString();
                String deleteSet = qs.getResource("?deleteSet").toString();

                ChangeSet cs = new ChangeSet(revisionGraph, nextRevision, addSet, deleteSet, changeSet);

                path.addRevisionToPathStart(cs);
                if (nextRevision.getRevisionURI().equals(targetRevision.getRevisionURI())) {
                    return path;
                }
                currentRevisionUri = nextResource;
                resultSet.reset();
            }
        }
        throw new InternalErrorException("Shortest path between revisions could not be found");
    }

}