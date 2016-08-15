package de.tud.plt.r43ples.management;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.P_ZeroOrMore1;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.RDF;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.revisionTree.Revision;
import de.tud.plt.r43ples.revisionTree.Tree;

/**
 * Rewrites SPARQL queries in order to reflect old revisions.
 * 
 * @author Markus Graube
 *
 */
public class SparqlRewriter {

	/** The logger. **/
	private static final Logger logger = Logger.getLogger(SparqlRewriter.class);

	private static final String rmo = "http://eatld.et.tu-dresden.de/rmo#";
	private static final String prov = "http://www.w3.org/ns/prov#";

	private static final Node rmo_Revision = NodeFactory.createURI(rmo + "Revision");
	private static final Node rmo_deleteSet = NodeFactory.createURI(rmo + "deleteSet");
	private static final Node rmo_addSet = NodeFactory.createURI(rmo + "addSet");
	private static final Node rmo_fullGraph = NodeFactory.createURI(rmo + "fullGraph");
	private static final Node rmo_references = NodeFactory.createURI(rmo + "references");
	private static final Node prov_wasDerivedFrom = NodeFactory.createURI(prov + "wasDerivedFrom");



	/** instance variables */
	private int statement_i = 0;
	private LinkedList<String> revisions = new LinkedList<String>();
	private LinkedList<String> graphs = new LinkedList<String>();
	private ExprList expression_list_revision_path = new ExprList();
	private Expr last_revision;
	private String revisionNumber;
	private String graphName; 
	
	public static String rewriteQuery(final String query_r43ples) throws InternalErrorException {
		SparqlRewriter sr = new SparqlRewriter();
		return sr.rewrite(query_r43ples);
	}
	
	
	
	/** updates last_revision and expression_list_revision_path by pulling first item of revisionNumber and graphName
	 * 
	 * @return false if revisionNumber is still a branch, otherwise true
	 */
	private boolean updateRevision(){
		revisionNumber = revisions.removeFirst();
		graphName = graphs.removeFirst();
		if (RevisionManagement.isBranch(graphName, revisionNumber))
			return false;
		else {
			String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
			Tree tree =  new Tree(revisionGraph);
			LinkedList<Revision> list = tree.getPathToRevision(revisionNumber);
			logger.debug("Path to revision: " + list.toString());
			last_revision = ExprUtils.nodeToExpr(NodeFactory.createURI(list.get(0).getRevisionUri()));
			list.removeLast();
			for (Revision ns : list) {
				expression_list_revision_path.add(ExprUtils.nodeToExpr(NodeFactory.createURI(ns.getRevisionUri())));
			}
			return true;
		}
	}
	
	public String rewrite(final String r43ples_query) throws InternalErrorException {
		
		final Pattern pattern1 = Pattern.compile("GRAPH\\s*<(?<graph>\\S*)>\\s*\\{", Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
		final Pattern pattern2 = Pattern.compile("GRAPH\\s*<(?<graph>\\S*)>\\s*REVISION\\s*\"(?<revision>\\S*)\"", Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
				
		
		Matcher m1 = pattern1.matcher(r43ples_query);
		String query_sparql = m1.replaceAll("GRAPH <$1> REVISION \"master\" {");
				
		Matcher m2 = pattern2.matcher(query_sparql);
		
		while (m2.find()) {
			String graphName = m2.group("graph");
			String referenceName = m2.group("revision").toLowerCase();
			
			String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
			String revisionNumber = RevisionManagement.getRevisionNumber(revisionGraph, referenceName);
			graphs.add(graphName);
			revisions.add(revisionNumber);

			query_sparql = m2.replaceFirst("GRAPH <"+graphName+">");
			m2 = pattern2.matcher(query_sparql);
		}

		// creates the Query
		Query query_new = QueryFactory.create(query_sparql);
		Element el_orginal = query_new.getQueryPattern();

		// Do the rewriting and store the modified elements
		Element el_modified = getRewrittenElement(el_orginal);
		
		// force distinct in order to avoid duplicate entries due to multiple graph joins 
		query_new.setDistinct(true);
		query_new.setQueryPattern(el_modified);
		query_sparql = query_new.serialize();
		logger.debug("Rewritten query: \n" + query_sparql);
		return query_sparql;
	}

	
	/**
	 * @param el_orginal
	 * @return rewritten element group
	 */
	private Element getRewrittenElement(final Element el_orginal) {
		if (el_orginal.getClass().equals(ElementNamedGraph.class)) {
			ElementNamedGraph ng_original = (ElementNamedGraph) el_orginal;
			ElementGroup eg_original = (ElementGroup) ng_original.getElement();
			if (updateRevision())
				return getRewrittenElement(eg_original);
			else {
				ElementNamedGraph ng_new;
				try {
					ng_new = new ElementNamedGraph(NodeFactory.createURI(RevisionManagement.getReferenceGraph(graphName, revisionNumber)), eg_original);
					return ng_new;
				} catch (InternalErrorException e) {
					e.printStackTrace();
					return null;
				}
			}
		} 
		else if (el_orginal.getClass().equals(ElementGroup.class)) {
			ElementGroup elementgroup = (ElementGroup) el_orginal;
			
			ElementGroup eg_modified = new ElementGroup();
			for (Element el : elementgroup.getElements()) {
				Element el_mod = getRewrittenElement(el);
				expand(eg_modified, el_mod);
			}
			return eg_modified;
		} 
		else if (el_orginal.getClass().equals(ElementMinus.class)) {
			ElementMinus elementMinus = (ElementMinus) el_orginal;
			ElementGroup elementgroup = (ElementGroup) elementMinus.getMinusElement();
			Element minusPart = getRewrittenElement(elementgroup);
			ElementMinus em = new ElementMinus(minusPart);
			return em;
		}
		else if (el_orginal.getClass().equals(ElementUnion.class)) {
			ElementUnion elementUnion = (ElementUnion) el_orginal;
			List<Element> elements = elementUnion.getElements();
			ElementUnion elementModified = new ElementUnion();
			for (Element el : elements)
				elementModified.addElement(getRewrittenElement(el));
			return elementModified;
		}
		else if (el_orginal.getClass().equals(ElementPathBlock.class)){
			ElementPathBlock epb = (ElementPathBlock) el_orginal;
			Iterator<TriplePath> itPatternElts = epb.patternElts();

			ElementGroup eg_modified = new ElementGroup();
			while (itPatternElts.hasNext()) {
				TriplePath triplePath = itPatternElts.next();
				statement_i += 1;
				ElementGroup eg = getRewrittenTriplePath(triplePath);
				expand(eg_modified, eg);
			}
			return eg_modified;
		}
		else {
			return el_orginal;
		}
	}

	/**
	 * @param triplePath
	 * @return rewritten triple path element
	 */
	private ElementGroup getRewrittenTriplePath(TriplePath triplePath) {
		{
			ElementGroup eg_modified = new ElementGroup();
			
			ElementTriplesBlock block_triple_path = new ElementTriplesBlock();
			block_triple_path.addTriple(triplePath.asTriple());
			

			Node g_delete_set_full_graph = Var.alloc("g_delete_set_full_graph_" + statement_i);
			Node g_add_set = Var.alloc("g_add_set_" + statement_i);
			Node g_revisiongraph = NodeFactory.createURI(RevisionManagement.getRevisionGraph(graphName));			
			
			Var var_r_delete_set = Var.alloc("r_delete_set_" + statement_i);
			Var var_r_add_set = Var.alloc("r_add_set_" + statement_i);

			ElementGroup eg_fullgraph = new ElementGroup();
			Node anon = NodeFactory.createAnon();
//			eg_fullgraph.addElement(new ElementBind( var_r_delete_set, ExprUtils.nodeToExpr(branch)));
			eg_fullgraph.addTriplePattern(new Triple(anon, rmo_references, var_r_delete_set));
			eg_fullgraph.addTriplePattern(new Triple(anon, rmo_fullGraph, g_delete_set_full_graph));
			eg_fullgraph.addElementFilter(new ElementFilter(new E_Equals(new ExprVar(var_r_delete_set), last_revision)));
			

			ElementUnion eg_union = new ElementUnion();
			eg_union.addElement(eg_fullgraph);			
			
			ElementGroup eg_revisiongraph = new ElementGroup();
			eg_revisiongraph.addElement(eg_union);

			ElementGroup eg_delete_set = new ElementGroup();
			eg_delete_set.addTriplePattern(new Triple(var_r_delete_set, RDF.type.asNode(), rmo_Revision));
			eg_delete_set.addTriplePattern(new Triple(var_r_delete_set, rmo_deleteSet, g_delete_set_full_graph));
			eg_delete_set.addElementFilter(new ElementFilter(new E_OneOf(new ExprVar(var_r_delete_set),
					expression_list_revision_path)));
			eg_union.addElement(eg_delete_set);

			
			ElementGroup eg_revisiongraph2 = new ElementGroup();
			ElementPathBlock ebp = new ElementPathBlock();	
			ebp.addTriplePath(new TriplePath(var_r_delete_set, new P_ZeroOrMore1(new P_Link(prov_wasDerivedFrom)),
					var_r_add_set));
			eg_revisiongraph2.addElement(ebp);
			eg_revisiongraph2.addTriplePattern(new Triple(var_r_add_set, RDF.type.asNode(), rmo_Revision));
			eg_revisiongraph2.addTriplePattern(new Triple(var_r_add_set, rmo_addSet, g_add_set));
			eg_revisiongraph2.addElementFilter(new ElementFilter(new E_OneOf(new ExprVar(var_r_add_set),
					expression_list_revision_path)));			
		
			ElementGroup eg_minus = new ElementGroup();
			eg_minus.addElement(new ElementNamedGraph(g_add_set, block_triple_path));
			eg_minus.addElement(new ElementNamedGraph(g_revisiongraph, eg_revisiongraph2));
			
			eg_modified.addElement(new ElementNamedGraph(g_delete_set_full_graph, block_triple_path));
			eg_modified.addElement(new ElementNamedGraph(g_revisiongraph, eg_revisiongraph));
			eg_modified.addElement(new ElementMinus(eg_minus));
			return eg_modified;
		}
	}
	
	private static ElementGroup expand(ElementGroup eg, final Element el) {
		if (el.getClass().equals(ElementGroup.class))
			for (Element el_new : ((ElementGroup) el).getElements())
				eg.addElement(el_new);
		else
			eg.addElement(el);
		return eg;
	}

}
