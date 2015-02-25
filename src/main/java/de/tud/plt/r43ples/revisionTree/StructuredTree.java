package de.tud.plt.r43ples.revisionTree;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;

public class StructuredTree {

	private List<Branch> branches;
	private List<Tag> tags;
	private List<Commit> commits;

	private static Map<Commit, String> commit_branch_tmp;
	private static Logger logger = Logger.getLogger(StructuredTree.class);

	private StructuredTree() {
		commits = new LinkedList<Commit>();
		branches = new LinkedList<Branch>();
		tags = new LinkedList<Tag>();
		commit_branch_tmp = new HashMap<Commit, String>();
	}

	public static StructuredTree getTreeOfGraph(String graph) {
		// new Tree
		StructuredTree t = new StructuredTree();
		t.updateCommits(graph);
		t.updateBranches(graph);
		t.updateTags(graph);

		// link commits to branches
		for(Commit c : t.commits) {
			//get branch of commit
			if (commit_branch_tmp.containsKey(c)) {
				Branch b = t.branches.get(t.branches.indexOf(new Branch(commit_branch_tmp.get(c))));
				c.setBranch(b);
			}
			else
				c.setBranch(t.branches.get(0));
		}
		return t;
	}

	private void updateBranches(String graph) {
		//query all branches
		String queryBranches = String.format(
				RevisionManagement.prefixes
						+ "SELECT ?branch ?title ?commit\n"
						+ "WHERE { GRAPH <%s> { \n"
						+ "?branch a rmo:Branch;\n"
						+ "rdfs:label ?title;\n"
						+ "rmo:references ?rev.\n"
						+ "?rev rmo:revisionOf <%s>.\n"
						+ "?commit a rmo:Commit;\n"
						+ "prov:generated ?rev.\n"
						+ "} }", Config.revision_graph, graph);

		ResultSet resultsBranches = TripleStoreInterfaceFactory.get().executeSelectQuery(queryBranches);

		while (resultsBranches.hasNext()) {
			QuerySolution sol = resultsBranches.next();
			Branch b = new Branch(sol.get("branch").toString(), sol.getLiteral("title").toString(), commits.get(commits
					.indexOf(new Commit(sol.get("commit").toString()))));
			branches.add(b);
		}
	}

	private void updateTags(String graph) {
		// query all tags
		String queryBranches = String.format(
				RevisionManagement.prefixes
						+ "SELECT ?tag ?title ?commit\n"
						+ "WHERE { GRAPH <%s> {\n"
						+ "?tag a rmo:Tag;\n"
						+ "rdfs:label ?title;\n"
						+ "rmo:references ?rev.\n"
						+ "?rev rmo:revisionOf <%s>.\n"
						+ "?commit a rmo:Commit;\n"
						+ "prov:generated ?rev.\n"
						+ "} }", Config.revision_graph, graph);

		ResultSet resultsTags = TripleStoreInterfaceFactory.get().executeSelectQuery(queryBranches);

		while (resultsTags.hasNext()) {
			QuerySolution sol = resultsTags.next();
			Tag t = new Tag(sol.get("tag").toString(), sol.getLiteral("title").toString(), commits.get(commits
					.indexOf(new Commit(sol.get("commit").toString()))));
			tags.add(t);
		}
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

	private void updateCommits(String graph) {
		// query all commits
		String queryCommits = String.format(
				RevisionManagement.prefixes
						+ "SELECT ?commit ?time ?prev ?next ?title ?authname ?branch\n"
						+ "WHERE { GRAPH <%s> {\n"
						+ "?commit a rmo:Commit;\n"
						+ "dc-terms:title ?title;\n"
						+ "prov:used ?reva;\n"
						+ "prov:generated ?revb;\n"
						+ "prov:atTime ?time;\n"
						+ "prov:wasAssociatedWith ?author.\n"
						+ "OPTIONAL { ?author rdfs:label ?authname. }\n"
						+ "?reva rmo:revisionNumber ?prev;"
						+ "rmo:revisionOf <%s>.\n"
						+ "?revb rmo:revisionNumber ?next."
						+ "OPTIONAL { ?revb rmo:revisionOfBranch ?branch. }\n"
						+ "} }",
				Config.revision_graph,
				graph);
		
		ResultSet resultsCommits = TripleStoreInterfaceFactory.get().executeSelectQuery(queryCommits);

		// generate list of commits
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		while (resultsCommits.hasNext()) {
			QuerySolution sol = resultsCommits.next();
			try {
				Commit commit = new Commit(sol.get("commit").toString());
				// commit can be multiple times in solution set because of
				// multiple "prov:used" attributes
				if (commits.contains(commit))
				{
					// if commit is already in list, just add new base revision
					// to existing commit
					commits.get(commits.indexOf(commit)).getBaseRevisions().add(sol.getLiteral("prev").getString());
				} else {
					// generate new commit object
					String authname = sol.getLiteral("authname") == null ? "" : sol.getLiteral("authname").getString();
					Commit newCommit = new Commit(
							sol.get("commit").toString(),
							sol.getLiteral("title").getString(),
							df.parse(sol.getLiteral("time").toString()),
							authname,
							sol.getLiteral("prev").getString(),
							sol.getLiteral("next").getString());
					// save branch uri in Map to set branch reference later
					// after branches were generated
					if (sol.get("branch") != null)
						commit_branch_tmp.put(newCommit, sol.get("branch").toString());
					commits.add(newCommit);
				}
			} catch (ParseException e) {
				logger.error("Commit could not be parsed!");
			}
		}

		// fill predecessor and successor lists of commits
		for (Commit c : commits) {
			for (Commit b : commits) {
				if (c.getBaseRevisions().contains(b.getNextRevision()))
					c.predecessor.add(b);
				else if (b.getBaseRevisions().contains(c.getNextRevision()))
					c.successors.add(b);
			}
		}

		// sort list of commits by order of their occurrence
		Collections.sort(commits);
	}
}
