package de.tud.plt.r43ples.visualisation;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.http.HttpException;

import att.grappa.Attribute;
import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.Node;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.InternalServerErrorException;


public class GraphVizVisualisation {
	
	public static String getGraphVizOutput(String namedGraph) throws IOException, HttpException {
		Graph graph =  new Graph("RevisionGraph of " + namedGraph);
		String query_nodes = String.format(RevisionManagement.prefixes
				+ "SELECT DISTINCT ?revision ?number "
				+ "FROM <%s> "
				+ "WHERE {"
				+ " ?revision a rmo:Revision;"
				+ "		rmo:revisionOf <%s>;"
				+ "		rmo:revisionNumber ?number."
//				+ " ?commit a rmo:Commit;"
//				+ "		rdfs:label ?label;"
//				+ "		?p ?date. "
				+ "}", Config.revision_graph, namedGraph );
		
		String result_nodes = TripleStoreInterface.executeQueryWithAuthorization(query_nodes);
		ResultSet resultSet_nodes = ResultSetFactory.fromXML(result_nodes);
		if (!resultSet_nodes.hasNext())
			throw new InternalServerErrorException("Specified graph '"+namedGraph +"' does not have any revision");
		
		while (resultSet_nodes.hasNext()) {
			QuerySolution qs = resultSet_nodes.next();
			String rev = qs.getResource("revision").toString();
			String number = qs.getLiteral("number").toString();
			Node newNode = new Node(graph, rev);
			newNode.setAttribute(Attribute.LABEL_ATTR, number+" | "+rev);
			newNode.setAttribute(Attribute.SHAPE_ATTR, Attribute.RECORD_SHAPE);
			graph.addNode(newNode);
			
		}		
		
		String query_edge = String.format(RevisionManagement.prefixes
				+ "SELECT DISTINCT ?revision ?next_revision "
				+ "FROM <%s> "
				+ "WHERE {"
				+ " ?revision a rmo:Revision;"
				+ "		rmo:revisionOf <%s>."
				+ "	?next_revision a rmo:Revision;"
				+ "		prov:wasDerivedFrom ?revision."
				+ "}", Config.revision_graph, namedGraph );
		
		String result_edge = TripleStoreInterface.executeQueryWithAuthorization(query_edge);
		ResultSet resultSet_edge = ResultSetFactory.fromXML(result_edge);
		while (resultSet_edge.hasNext()) {
			QuerySolution qs = resultSet_edge.next();
			String rev = qs.getResource("revision").toString();
			String next = qs.getResource("next_revision").toString();
			Node newNode = graph.findNodeByName(rev);
			Node nextNode = graph.findNodeByName(next);
			graph.addEdge(new Edge(graph, newNode, nextNode));
		}
		
		String query_reference = String.format(RevisionManagement.prefixes
				+ "SELECT DISTINCT ?revision ?label "
				+ "FROM <%s> "
				+ "WHERE {"
				+ " ?revision a rmo:Revision;"
				+ "		rmo:revisionOf <%s>."
				+ "	?reference a rmo:Reference;"
				+ "		rmo:references ?revision;"
				+ "		rdfs:label ?label."
				+ "}", Config.revision_graph, namedGraph );
		
		String result_reference = TripleStoreInterface.executeQueryWithAuthorization(query_reference);
		ResultSet resultSet_reference = ResultSetFactory.fromXML(result_reference);
		while (resultSet_reference.hasNext()) {
			QuerySolution qs = resultSet_reference.next();
			String rev = qs.getResource("revision").toString();
			String reference = qs.getLiteral("label").toString();
			Node refNode = new Node(graph, reference);
			refNode.setAttribute(Attribute.SHAPE_ATTR, Attribute.DIAMOND_SHAPE);
			Node revNode = graph.findNodeByName(rev);
			graph.addEdge(new Edge(graph, refNode, revNode));
		}
		
		
		StringWriter sw = new StringWriter();
		graph.printGraph(sw);
	    return sw.toString();
	}

	public static String getGraphVizHtmlOutput(String graphName) throws IOException, HttpException{
		String html = ResourceManagement.getContentFromResource("webapp/graphvisualisation.html");
		String content = String.format(html, getGraphVizOutput(graphName));
		return content;
	}	
}
