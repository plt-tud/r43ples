package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class SparqlRewriter {
	
	/** The logger. **/
	private static Logger logger = Logger.getLogger(RevisionManagement.class);

	public static String rewriteQuery(String query) throws HttpException, IOException {

		// create pattern for FROM clause
		Pattern pattern = Pattern.compile("FROM\\s*<(?<graph>.*)>\\s*#REVISION\\s*\"(?<revision>.*)\"");
		
		Matcher m = pattern.matcher(query);
		while (m.find()){
		    String graphName = m.group("graph");
		    String revisionNumber = m.group("revision");
		    
		    LinkedList<String> list =  RevisionManagement.getRevisionTree(graphName).getPathToRevision(revisionNumber);
			logger.info("Path to revision: " + list.toString());				
			
			
			
			
			
			
		}
		
		// creates the Query
		Query qe = QueryFactory.create(query);
		ElementGroup originalEG = (ElementGroup) qe.getQueryPattern();
				
		// stores the modified elements
		ElementGroup modifiedEG = new ElementGroup();
				
		ElementTriplesBlock block = new ElementTriplesBlock();
				

		int graph_i = 1;
				
		for (Element element : originalEG.getElements()) {
					
						
						ElementPathBlock epb = (ElementPathBlock) element;
						Iterator<TriplePath> itPatternElts = epb.patternElts();
						
						while (itPatternElts.hasNext())
						{
							try {
								TriplePath next = itPatternElts.next();
								Node graph = Var.alloc("graph"+graph_i);
								
								//add pattern match
								//block.addTriple(Triple.create(Node.createURI(this.role), Node.createURI(ac+"canSee"), graph));
								
								ElementTriplesBlock block2 = new ElementTriplesBlock();
								block2.addTriple(next.asTriple());
								ElementNamedGraph ng = new ElementNamedGraph(graph, block2 );
								
								modifiedEG.addElement(ng);
								graph_i += 1;
						
							} catch (ClassCastException e) {
						
							}
						}
				}		
				
				ElementNamedGraph accessGraph = new ElementNamedGraph(Var.alloc("g_ac"), block);
				
				//add Elements to body_mod_result in the right order
				ElementGroup resultEG = new ElementGroup();
				resultEG.addElement(accessGraph);
				Iterator<Element> itElement = modifiedEG.getElements().iterator();
				while (itElement.hasNext()) {
					Element nextElement = itElement.next();
					resultEG.addElement(nextElement);
				}
				
				
				qe.setQueryPattern(resultEG);
				query = qe.serialize();
		
		return query;
	}
}
