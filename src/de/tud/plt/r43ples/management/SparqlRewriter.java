package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.P_OneOrMore1;
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

public class SparqlRewriter {

	/** The logger. **/
	private static Logger logger = Logger.getLogger(RevisionManagement.class);

	private static String rmo = "http://eatld.et.tu-dresden.de/rmo#";
	private static String prov = "http://www.w3.org/ns/prov#";

	public static String rewriteQuery(final String query_r43ples)
			throws HttpException, IOException {

		Pattern pattern = Pattern
				.compile("FROM\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>.*)\"");

		ExprList expression_list_revision_path = new ExprList();
		ExprList expression_list_last_revision = new ExprList();
//		Node lastRevision = null;
		
		
		Matcher m = pattern.matcher(query_r43ples);
		String query_sparql = query_r43ples;
		while (m.find()){
			String graphName = m.group("graph");
			String revisionNumber = m.group("revision");
			m.reset();
	
			LinkedList<String> list = RevisionManagement.getRevisionTree(graphName)
					.getPathToRevisionWithUri(revisionNumber);
			logger.info("Path to revision: " + list.toString());
//	
//			// Already reference revision -> only remove REVISON keyword
//			if (list.size() == 1) {
//				return m.replaceAll("FROM <${graph}>");
//			}
	
			
//			lastRevision = NodeFactory.createURI(list.get(0));
			
			expression_list_last_revision.add(ExprUtils.nodeToExpr(NodeFactory
					.createURI(list.get(0))));
			list.removeLast();
			for (String string : list) {
				expression_list_revision_path.add(ExprUtils.nodeToExpr(NodeFactory
						.createURI(string)));
			}
	
			query_sparql = m.replaceFirst("");
			m = pattern.matcher(query_sparql);
		}
		
		Node rmo_Revision = NodeFactory.createURI(rmo + "Revision");
		Node rmo_deltaRemoved = NodeFactory.createURI(rmo + "deltaRemoved");
		Node rmo_deltaAdded = NodeFactory.createURI(rmo + "deltaAdded");
		Node rmo_fullGraph = NodeFactory.createURI(rmo + "fullGraph");
		Node rmo_references = NodeFactory.createURI(rmo + "references");
		Node prov_wasDerivedFrom = NodeFactory.createURI(prov
				+ "wasDerivedFrom");

		// creates the Query
		Query qe = QueryFactory.create(query_sparql);
		ElementGroup eg_orginal = (ElementGroup) qe.getQueryPattern();

		// stores the modified elements
		ElementGroup eg_modified = new ElementGroup();

		int statement_i = 0;

		for (Element element : eg_orginal.getElements()) {
			try {
				ElementPathBlock epb = (ElementPathBlock) element;
				Iterator<TriplePath> itPatternElts = epb.patternElts();

				while (itPatternElts.hasNext()) {
					TriplePath next = itPatternElts.next();
					statement_i += 1;

					Node var_g1 = Var.alloc("g" + statement_i);
					Node var_ref1 = Var.alloc("ref" + statement_i);
					Node var_rev1 = Var.alloc("rev" + statement_i);
					Node var_rg1 = Var.alloc("rg" + statement_i);
					Node var_rm1 = Var.alloc("rm" + statement_i);
					Node var_gm1 = Var.alloc("gm" + statement_i);
					Node var_rm1_old = Var.alloc("rm" + statement_i + "_old");
					Node var_gm1_old = Var.alloc("gm" + statement_i + "_old");

					ElementTriplesBlock block_g1 = new ElementTriplesBlock();
					block_g1.addTriple(next.asTriple());
					ElementNamedGraph ng = new ElementNamedGraph(var_g1,
							block_g1);

					ElementGroup eg1 = new ElementGroup();
					eg1.addTriplePattern(new Triple(var_ref1, rmo_references,
							var_rev1));
					eg1.addTriplePattern(new Triple(var_ref1, rmo_fullGraph,
							var_g1));
					eg1.addElementFilter(new ElementFilter(
							new E_OneOf(new ExprVar(var_rev1),
									expression_list_last_revision)));

					eg_modified.addElement(ng);
					eg_modified.addElement(eg1);
					
					if (!expression_list_revision_path.isEmpty()){
						ElementGroup eg2 = new ElementGroup();
						eg2.addTriplePattern(new Triple(var_rg1, RDF.type.asNode(),
								rmo_Revision));
						eg2.addTriplePattern(new Triple(var_rg1, rmo_deltaRemoved,
								var_g1));
						// add filter
						// FILTER (?rg1 IN
						// (<http://test.com/r43ples-dataset-revision-4>,
						// <http://test.com/r43ples-dataset-revision-3>))
						eg2.addElementFilter(new ElementFilter(
								new E_OneOf(new ExprVar(var_rg1),
										expression_list_revision_path)));
						ElementUnion union = new ElementUnion();
						union.addElement(eg2);
	
						// First Minus part
						// GRAPH ?gm1 { ?s ?p ?o }
						// ?rm1 a rmo:Revision.
						// ?rm1 rmo:deltaAdded ?gm1.
						// FILTER (?rm1 IN
						// (<http://test.com/r43ples-dataset-revision-4>,
						// <http://test.com/r43ples-dataset-revision-3>))
						ElementGroup eg_minus = new ElementGroup();
						ElementMinus minus = new ElementMinus(eg_minus);
						ElementNamedGraph ng1 = new ElementNamedGraph(var_gm1,
								block_g1);
						eg_minus.addElement(ng1);
						eg_minus.addTriplePattern(new Triple(var_rm1, RDF.type
								.asNode(), rmo_Revision));
						eg_minus.addTriplePattern(new Triple(var_rm1,
								rmo_deltaAdded, var_gm1));
	
						ElementGroup eg_innerminus = new ElementGroup();
						// GRAPH ?gm1_old {?s ?p ?o.}
						// ?rm1_old a rmo:Revision.
						// ?rm1 prov:wasDerivedFrom+ ?rm1_old.
						// ?rm1_old rmo:deltaRemoved ?gm1_old.
						// FILTER (?rm1_old IN
						// (<http://test.com/r43ples-dataset-revision-4>,
						// <http://test.com/r43ples-dataset-revision-3>))
						ElementMinus inner_minus = new ElementMinus(eg_innerminus);
						ElementNamedGraph ng2 = new ElementNamedGraph(var_gm1_old,
								block_g1);
						eg_innerminus.addElement(ng2);
	
						ElementPathBlock ebp = new ElementPathBlock();
						ebp.addTriplePath(new TriplePath(var_rm1_old, new P_Link(
								RDF.type.asNode()), rmo_Revision));
						ebp.addTriplePath(new TriplePath(var_rm1_old, new P_Link(
								rmo_deltaRemoved), var_gm1_old));
						ebp.addTriplePath(new TriplePath(var_rm1, new P_OneOrMore1(
								new P_Link(prov_wasDerivedFrom)), var_rm1_old));
						eg_innerminus.addElement(ebp);
	
						eg_innerminus.addElementFilter(new ElementFilter(
								new E_OneOf(new ExprVar(var_rm1_old),
										expression_list_revision_path)));
						eg_minus.addElementFilter(new ElementFilter(
								new E_OneOf(new ExprVar(var_rm1),
										expression_list_revision_path)));
						eg_minus.addElement(inner_minus);
						eg_modified.addElement(union);
						eg_modified.addElement(minus);
					}
				}
			} catch (ClassCastException e) {
				eg_modified.addElement(element);
			}
		}

		qe.setDistinct(true);
		qe.setQueryPattern(eg_modified);
		query_sparql = qe.serialize();
		logger.info("Rewritten query: \n" + query_sparql);
		return query_sparql;
	}

}
