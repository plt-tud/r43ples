package de.tud.plt.r43ples.management;

import java.util.regex.Pattern;


/**
 * Provides information of a request received by R43ples.
 *
 * @author Markus Graube
 */
public class R43plesRequest {

	/** The pattern modifier. **/
	private final static int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	/** Pattern for SELECT, ASK and CONSTRUCT queries. **/
	private final Pattern patternSelectAskConstructQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
    /** Pattern for UPDATE queries. **/
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
    /** Pattern for CREATE GRAPH queries. **/
	private final Pattern patternCreateGraph = Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
    /** Pattern for DROP GRAPH queries. **/
	private final Pattern patternDropGraph = Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
    /** Pattern for BRANCH or TAG queries. **/
	private final Pattern patternBranchOrTagQuery = Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")\\s*TO\\s*\"(?<name>[^\"]*)\"",
			patternModifier);
    /** Pattern for MERGE queries. **/
	private final Pattern patternMergeQuery =  Pattern.compile(
			"(MERGE|MERGE FF|REBASE)\\s*(AUTO|MANUAL)?\\s*GRAPH\\s*<([^>]*?)>\\s*(SDD\\s*<([^>]*?)>)?\\s*BRANCH\\s*\"([^\"]*?)\"\\s*INTO\\s*\"([^\"]*?)\"",
			patternModifier);
	
	/** The original query received by R43ples. **/
	public final String query_r43ples;
	/** The format of the original query. **/
	public final String format;
	/** The resulting SPARQL query. **/
	public String query_sparql;


    /**
     * The constructor.
     *
     * @param query the query received by R43ples
     * @param format the query format
     */
    public R43plesRequest(final String query, final String format) {
		this.query_r43ples = query;
		this.format = format;
		this.query_sparql = query;
	}

    /**
     * Test if the SPARQL query is a SELECT, ASK or CONSTRUCT query.
     *
     * @return true if the query is a SELECT, ASK or CONSTRUCT query.
     */
    public boolean isSelectAskConstructQuery(){
        return patternSelectAskConstructQuery.matcher(query_sparql).find();
	}

    /**
     * Test if the SPARQL query is an UPDATE query.
     *
     * @return true if the query is an UPDATE query.
     */
	public boolean isUpdateQuery() {
        return patternUpdateQuery.matcher(query_sparql).find();
	}

    /**
     * Test if the SPARQL query is a CREATE GRAPH query.
     *
     * @return true if the query is a CREATE GRAPH query.
     */
	public boolean isCreateGraphQuery() {
		return patternCreateGraph.matcher(query_sparql).find();
	}

    /**
     * Test if the SPARQL query is a DROP GRAPH query.
     *
     * @return true if the query is a DROP GRAPH query.
     */
	public boolean isDropGraphQuery() {
		return patternDropGraph.matcher(query_sparql).find();
	}

    /**
     * Test if the SPARQL query is a BRANCH or TAG query.
     *
     * @return true if the query is a BRANCH or TAG query.
     */
	public boolean isBranchOrTagQuery() {
		return patternBranchOrTagQuery.matcher(query_sparql).find();
	}

    /**
     * Test if the SPARQL query is a MERGE query.
     *
     * @return true if the query is a MERGE query.
     */
	public boolean isMergeQuery() {
		return patternMergeQuery.matcher(query_sparql).find();
	}

}