package de.tud.plt.r43ples.management;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class R43plesCommit extends R43plesRequest{
	
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);

	public String user;
	public String message;
	
	public R43plesCommit(final R43plesRequest request){
		super(request.query_r43ples, request.format);
		this.query_sparql = request.query_sparql;
		this.extractUser();
		this.extractMessage();
	}
	
	private String extractUser() {
		Matcher userMatcher = patternUser.matcher(this.query_sparql);
		if (userMatcher.find()) {
			this.user = userMatcher.group("user");
			this.query_sparql = userMatcher.replaceAll("");
		}
		return this.user;
	}

	private String extractMessage() {
		Matcher messageMatcher = patternCommitMessage.matcher(this.query_sparql);
		if (messageMatcher.find()) {
			this.message = messageMatcher.group("message");
			this.query_sparql = messageMatcher.replaceAll("");
		}	
		return this.message;
	}
	
}
