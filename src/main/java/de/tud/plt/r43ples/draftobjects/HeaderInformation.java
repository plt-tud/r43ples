package de.tud.plt.r43ples.draftobjects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileUtils;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderInformation {

    /** The logger. **/
    private Logger logger = Logger.getLogger(HeaderInformation.class);


    /** Creates an RDF description for the revision tree of the graphs specified in the given SPARQL query
     * @param query SPARQL query
     * @return RDF string containing information for graphs specified in query
     */
    public String getResponseHeaderFromQuery(String query) {
        final Pattern patternGraph = Pattern.compile(
                "(GRAPH|FROM|INTO)\\s*<(?<graph>[^>]*)>\\s*",
                Pattern.CASE_INSENSITIVE);

        StringBuilder graphNames = new StringBuilder();
        Matcher m = patternGraph.matcher(query);
        m.find();
        while (!m.hitEnd()) {
            String graphName = m.group("graph");
            graphNames.append("<"+graphName+">");
            m.find();
            if (!m.hitEnd())
                graphNames.append(", ");
        }
        String names = graphNames.toString();
        String result = getResponseHeader(names);
        return result;

    }

    protected String getResponseHeader(String graphList) {
        String queryConstruct = String.format(
                    "PREFIX rmo:	<http://eatld.et.tu-dresden.de/rmo#> "
                        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> %n"
                        + "CONSTRUCT {"
                        + " ?ref a ?type;"
                        + "		rdfs:label ?label;"
                        + "		rmo:references ?rev."
                        + " ?rev rmo:revisionNumber ?number . %n"
                        + "} %n"
                        + "WHERE {"
                        + " GRAPH <%s> {"
                        + "   ?graph a rmo:Graph; rmo:hasRevisionGraph ?revisionGraph."
                        + "   FILTER (?graph IN (%s))"
                        + " }"
                        + " GRAPH ?revisionGraph { "
                        + " ?ref a ?type;"
                        + "		rdfs:label ?label;%n"
                        + "		rmo:references ?rev."
                        + " ?rev rmo:revisionNumber ?number . %n"
                        + "FILTER (?type IN (rmo:Tag, rmo:Master, rmo:Branch)) %n"
                        + "} }", Config.revision_graph, graphList);
        String header = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryConstruct, FileUtils.langTurtle);
        return header;
    }

    protected void checkUpToDate(final String clientRevisionInformation, final String sparqlQuery) throws OutdatedException {

        Model clientModel = JenaModelManagement.readTurtleStringToJenaModel(clientRevisionInformation);

        String recentRevisionInformation = this.getResponseHeaderFromQuery(sparqlQuery);

        Model serverModel = JenaModelManagement.readTurtleStringToJenaModel(recentRevisionInformation);

        if (!clientModel.isIsomorphicWith(serverModel)) {
             throw new OutdatedException(clientModel, serverModel);
        }
    }

}
