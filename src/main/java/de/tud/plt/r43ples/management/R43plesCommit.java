package de.tud.plt.r43ples.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides information of a commit.
 *
 * @author Markus Graube
 */
public class R43plesCommit extends R43plesRequest{

	/** The pattern modifier. **/
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	/** The pattern to extract the user name. **/
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	/** The pattern to extract the commit message. **/
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);
	/** The associated user name of the commit. **/
	public String user;
	/** The message of the commit. **/
	public String message;


	/**
	 * The constructor.
	 *
	 * @param request the request received by R43ples
	 */
	public R43plesCommit(final R43plesRequest request){
		super(request.query_r43ples, request.format);
		this.query_sparql = request.query_sparql;
		this.extractUser();
		this.extractMessage();
	}

	/**
	 * Extracts the user name out of the given query.
	 *
	 * @return the extracted user name
	 */
	private String extractUser() {
		Matcher userMatcher = patternUser.matcher(this.query_sparql);
		if (userMatcher.find()) {
			this.user = userMatcher.group("user");
			this.query_sparql = userMatcher.replaceAll("");
		}
		return this.user;
	}

	/**
	 * Extracts the message out of the given query.
	 *
	 * @return the extracted message
	 */
	private String extractMessage() {
		Matcher messageMatcher = patternCommitMessage.matcher(this.query_sparql);
		if (messageMatcher.find()) {
			this.message = messageMatcher.group("message");
			this.query_sparql = messageMatcher.replaceAll("");
		}	
		return this.message;
	}
	
}