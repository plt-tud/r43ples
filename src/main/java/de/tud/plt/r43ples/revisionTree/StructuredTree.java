package de.tud.plt.r43ples.revisionTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;

public class StructuredTree {

	private List<Branch> branches;
	private List<Tag> tags;
	private List<Commit> commits;

	private StructuredTree() {
		commits = new ArrayList<Commit>();
	}

	public static StructuredTree getTreeOfGraph(String graph) throws IOException, HttpException {
		//new Tree
		StructuredTree t = new StructuredTree();
		
		//query all commits
		String queryCommits = String.format(
				  RevisionManagement.prefixes
				+ "SELECT ?commit ?prev ?next ?title ?authname\n"
				+ "FROM <%s>\n"
				+ "WHERE {\n"
				+ "?commit a rmo:Commit;\n"
				+ "dc-terms:title ?title;\n"
				+ "prov:used ?reva;\n"
				+ "prov:generated ?revb;\n"
				+ "prov:wasAssociatedWith ?author.\n"
				+ "?author rdfs:label ?authname.\n"
				+ "?reva rmo:revisionNumber ?prev;"
				+ "rmo:revisionOf <%s>.\n"
				+ "?revb rmo:revisionNumber ?next.\n"
				+ "}",
				Config.revision_graph,
				graph);
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryCommits, "XML");
		ResultSet resultsCommits = ResultSetFactory.fromXML(resultSparql);
		
	//generate list of commits
		while(resultsCommits.hasNext()) {
			QuerySolution sol = resultsCommits.next();
			Commit commit = new Commit(
					sol.getLiteral("title").getString(),
					sol.getLiteral("authname").getString(),
					sol.getLiteral("prev").getString(),
					sol.getLiteral("next").getString());
			t.commits.add(commit);
		}
		
		//fill predecessor and successor lists of commits
		for(Commit c : t.commits) {
			for(Commit b : t.commits) {
				if(c.getBaseRevision().equals(b.getNextRevision()))
					c.Predecessors.add(b);
				else if(c.getNextRevision().equals(b.getBaseRevision()))
					c.Successors.add(b);
			}
		}
		
		return t;
	}

}
