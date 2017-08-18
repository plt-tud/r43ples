package de.tud.plt.r43ples.optimization;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.Path;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class implements the PathCalculationInterface interface and provides simple not optimized algorithms to calculate paths between revisions.
 *
 * @author Stephan Hensel
 *
 */
public class SimplePathCalculation implements PathCalculationInterface {

    /** The logger. **/
    private Logger logger = Logger.getLogger(SimplePathCalculation.class);

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor.
     */
    protected SimplePathCalculation() {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
    }


    /**
     * Get the path to the nearest revision which has a full graph.
     *
     * @param revisionGraph the revision graph
     * @param revision revision where the search should start
     * @return path containing all revisions from start revision to next revision with a full graph
     * @throws InternalErrorException
     */
    @Override
    public Path getPathToRevisionWithFullGraph(RevisionGraph revisionGraph, Revision revision) throws InternalErrorException {
        logger.info("Get path to full graph revision starting from revision " + revision.getRevisionIdentifier());
        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision "
                + "WHERE { "
                + "    GRAPH <%1$s> {"
                + "        ?revision prov:wasDerivedFrom* <%2$s> ."
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

            Path path = this.getPathBetweenStartAndTargetRevision(revisionGraph, revision, revision_end);
            if (bestPath==null || bestPath.getRevisionPath().size()> path.getRevisionPath().size()){
            	this.logger.info("new best path: "+ path.getRevisionPath().size());
                bestPath = path;
            }
        }
        return bestPath;
    }




    /**
     * Get the common revision of the specified revisions which has the shortest path to the two.
     * To ensure wise results the revisions should be terminal branch nodes.
     *
     * @param revisionGraph the revision graph
     * @param revision1 the first revision should be a terminal branch node
     * @param revision2 the second revision should be a terminal branch node
     * @return the nearest common revision
     * @throws InternalErrorException
     */
    @Override
    public Revision getCommonRevisionWithShortestPath(RevisionGraph revisionGraph, Revision revision1, Revision revision2) throws InternalErrorException {
        logger.info("Get the common revision of revision " + revision1.getRevisionIdentifier() + " and revision " + revision2.getRevisionIdentifier() + " which has the shortest path.");
        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision "
                + "WHERE { "
                + "    GRAPH <%s> {"
                + "        <%2$s> prov:wasDerivedFrom+ ?revision ."
                + "        <%3$s> prov:wasDerivedFrom+ ?revision ."
                + "        ?next prov:wasDerivedFrom ?revision."
                + "        FILTER NOT EXISTS {"
                + "            <%2$s> prov:wasDerivedFrom+ ?next ."
                + "            <%3$s> prov:wasDerivedFrom+ ?next ."
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
     *
     * @param revisionGraph the revision graph
     * @param startRevision the start revision
     * @param targetRevision the target revision
     * @return path containing all revisions from start revision to target revision
     */
    @Override
    public Path getPathBetweenStartAndTargetRevision(RevisionGraph revisionGraph, Revision startRevision, Revision targetRevision) throws InternalErrorException {
        logger.info("Calculate the shortest path from revision " + startRevision.getRevisionIdentifier() + " to " + targetRevision.getRevisionIdentifier() + ".");

        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?revision ?previousRevision %n"
                + "WHERE { %n"
                + "	GRAPH <%s> { %n"
                + "		<%s> prov:wasDerivedFrom* ?revision."
                + "		?revision prov:wasDerivedFrom* <%s>."
                + "		OPTIONAL{?revision prov:wasDerivedFrom ?previousRevision}"
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
            if (currentPathElement.equals(startRevision)) {
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

}