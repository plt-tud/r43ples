package de.tud.plt.r43ples.management;

import java.util.regex.Pattern;


public class R43plesRequest {
	
private final static int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	
	private final Pattern patternSelectAskConstructQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternCreateGraph = Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternDropGraph = Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternBranchOrTagQuery = Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")\\s*TO\\s*\"(?<name>[^\"]*)\"",
			patternModifier);

	private final Pattern patternMergeQuery =  Pattern.compile(
			"(MERGE|MERGE FF|REBASE)\\s*(AUTO|MANUAL)?\\s*GRAPH\\s*<([^>]*?)>\\s*(SDD\\s*<([^>]*?)>)?\\s*BRANCH\\s*\"([^\"]*?)\"\\s*INTO\\s*\"([^\"]*?)\"",
			patternModifier);
	
	
	public final String query_r43ples;
	public final String format;
	public String query_sparql;
	
	public R43plesRequest(final String query, final String format) {
		this.query_r43ples = query;
		this.format = format;
		this.query_sparql = query;
	}
	
	public boolean isSelectAskConstructQuery(){
	  return patternSelectAskConstructQuery.matcher(query_sparql).find();
	}
	
	public boolean isUpdateQuery() {
		return patternUpdateQuery.matcher(query_sparql).find();
	}
	
	public boolean isCreateGraphQuery() {
		return patternCreateGraph.matcher(query_sparql).find();
	}
	
	public boolean isDropGraphQuery() {
		return patternDropGraph.matcher(query_sparql).find();	
	}
	
	public boolean isBranchOrTagQuery() {
		return patternBranchOrTagQuery.matcher(query_sparql).find();
	}
	
	public boolean isMergeQuery() {
		return patternMergeQuery.matcher(query_sparql).find();
	}
}
