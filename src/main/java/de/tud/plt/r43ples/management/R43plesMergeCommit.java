package de.tud.plt.r43ples.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;

public class R43plesMergeCommit extends R43plesCommit {
	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(R43plesMergeCommit.class);
	
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	private final Pattern patternMergeQuery = Pattern.compile(
			"(?<action>MERGE|REBASE|MERGE FF)\\s*(?<type>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(?<with>\\s*WITH\\s*\\{(?<triples>.*)\\})?",
			patternModifier);
	public final String triples;
	public final String branchNameA;
	public final String branchNameB;
	public final String sdd;
	public final String graphName;
	public final String type;
	public final String action;
	public final boolean with;

	public R43plesMergeCommit(R43plesRequest request) throws InternalErrorException {
		super(request);
		Matcher m = patternMergeQuery.matcher(request.query_sparql);
		if (!m.find())
			throw new InternalErrorException("Error in query: " + request.query_sparql);
		
		action = m.group("action");
		type = m.group("type");
		graphName = m.group("graph");
		sdd = m.group("sdd");
		branchNameA = m.group("branchNameA").toLowerCase();
		branchNameB = m.group("branchNameB").toLowerCase();
		with = m.group("with")!=null;
		triples = m.group("triples");
		
		logger.debug("type: " + type);
		logger.debug("graph: " + graphName);
		logger.debug("sdd: " + sdd);
		logger.debug("branchNameA: " + branchNameA);
		logger.debug("branchNameB: " + branchNameB);
		logger.debug("with: " + with);
		logger.debug("triples: " + triples);
	}
	
	
	public R43plesMergeCommit(final String graphName,
			final String branchNameA,
			final String branchNameB,
			final String user,
			final String message,
			final String format
			) {
		super(new R43plesRequest("", format));
		this.graphName = graphName;
		this.branchNameA = branchNameA;
		this.branchNameB = branchNameB;
		this.user = user;
		this.message = message;
		this.sdd = null;
		this.action = null;
		this.triples = null;
		this.type = null;
		this.with = false;
	}



	


}
