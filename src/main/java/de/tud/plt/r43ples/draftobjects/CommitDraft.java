package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new commit.
 *
 * @author Stephan Hensel
 */
public class CommitDraft {

	/** The logger. **/
	private Logger logger = Logger.getLogger(CommitDraft.class);

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

	/** The corresponding R43ples request. **/
	private R43plesRequest request;
	/** The associated user name of the commit. **/
	private String user;
	/** The message of the commit. **/
	private String message;
	/** The time stamp of the commit. **/
	private Date timeStamp;

	// Dependencies
	/** The triplestore interface to use. **/
	protected TripleStoreInterface tripleStoreInterface;


	/**
	 * The constructor.
	 *
	 * @param request the request received by R43ples
	 */
	public CommitDraft(R43plesRequest request){
		// Dependencies
		this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

		this.request = request;
		if (request != null) {
			this.extractUser();
			this.extractMessage();
		}
		this.timeStamp = new Date();
	}

	/**
	 * Extracts the user name out of the given query.
	 *
	 * @return the extracted user name
	 */
	private String extractUser() {
		Matcher userMatcher = patternUser.matcher(request.query_sparql);
		if (userMatcher.find()) {
			user = userMatcher.group("user");
			request.query_sparql = userMatcher.replaceAll("");
		}
		return user;
	}

	/**
	 * Extracts the message out of the given query.
	 *
	 * @return the extracted message
	 */
	private String extractMessage() {
		Matcher messageMatcher = patternCommitMessage.matcher(request.query_sparql);
		if (messageMatcher.find()) {
			message = messageMatcher.group("message");
			request.query_sparql = messageMatcher.replaceAll("");
		}	
		return message;
	}

	/**
	 * Formats a data as xsd:DateTime
	 *
	 * @param date the date to format
	 * @return current date formatted as xsd:DateTime
	 */
	private String formatDateString(Date date) {
		// Create current time stamp
		DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH:mm:ss.SSS");
		String dateString = df.format(date);
		logger.debug("Time stamp created: " + dateString);
		return dateString;
	}

	/**
	 * Get the corresponding R43ples request.
	 *
	 * @return the corresponding R43ples request
	 */
	protected R43plesRequest getRequest() {
		return request;
	}

	/**
	 * Get the associated user name of the commit.
	 *
	 * @return the associated user name of the commit
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Set the associated user name of the commit.
	 *
	 * @param user the associated user name of the commit
	 */
	protected void setUser(String user) {
		this.user = user;
	}

	/**
	 * Get the message of the commit.
	 *
	 * @return the message of the commit
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message of the commit.
	 *
	 * @param message the message of the commit
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Get the time stamp of the commit.
	 *
	 * @return the time stamp of the commit
	 */
	public String getTimeStamp() {
		return formatDateString(timeStamp);
	}

	/**
	 * Get the triplestore interface to use.
	 *
	 * @return the triplestore interface to use
	 */
	public TripleStoreInterface getTripleStoreInterface() {
		return tripleStoreInterface;
	}

}