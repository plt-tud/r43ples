package de.tud.plt.r43ples.revisionTree;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

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

	private static Logger logger = Logger.getLogger(StructuredTree.class);

	private StructuredTree() {
		commits = new ArrayList<Commit>();
	}

	public static StructuredTree getTreeOfGraph(String graph) throws IOException, HttpException {
		// new Tree
		StructuredTree t = new StructuredTree();

		// query all commits
		String queryCommits = String.format(
				RevisionManagement.prefixes
						+ "SELECT ?commit ?time ?prev ?next ?title ?authname\n"
						+ "FROM <%s>\n"
						+ "WHERE {\n"
						+ "?commit a rmo:Commit;\n"
						+ "dc-terms:title ?title;\n"
						+ "prov:used ?reva;\n"
						+ "prov:generated ?revb;\n"
						+ "prov:atTime ?time;\n"
						+ "prov:wasAssociatedWith ?author.\n"
						+ "OPTIONAL { ?author rdfs:label ?authname. }\n"
						+ "?reva rmo:revisionNumber ?prev;"
						+ "rmo:revisionOf <%s>.\n"
						+ "?revb rmo:revisionNumber ?next.\n"
						+ "}",
				Config.revision_graph,
				graph);
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryCommits, "XML");
		ResultSet resultsCommits = ResultSetFactory.fromXML(resultSparql);

		// generate list of commits
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		while (resultsCommits.hasNext()) {
			QuerySolution sol = resultsCommits.next();
			try {
				String authname = sol.getLiteral("authname") == null ? "" : sol.getLiteral("authname").getString();
				Commit commit = new Commit(
						sol.get("commit").toString(),
						sol.getLiteral("title").getString(),
						df.parse(sol.getLiteral("time").toString()),
						authname,
						sol.getLiteral("prev").getString(),
						sol.getLiteral("next").getString());
				if (t.commits.contains(commit))
				{
					t.commits.get(t.commits.indexOf(commit)).getBaseRevisions().add(sol.getLiteral("prev").getString());
				} else
					t.commits.add(commit);
			} catch (ParseException e) {
				logger.error("Commit could not be parsed!", e);
				e.printStackTrace();
			}
		}

		// fill predecessor and successor lists of commits
		for (Commit c : t.commits) {
			for (Commit b : t.commits) {
				if (c.getBaseRevisions().contains(b.getNextRevision()))
					c.Predecessors.add(b);
				else if (b.getBaseRevisions().contains(c.getNextRevision()))
					c.Successors.add(b);
			}
		}

		// sort list of commits by order of their occurrence
		Collections.sort(t.commits);

		return t;
	}

	public List<Commit> getCommits()
	{
		return commits;
	}

	public List<Branch> getBranches()
	{
		return branches;
	}

	public List<Tag> getTags()
	{
		return tags;
	}
}
