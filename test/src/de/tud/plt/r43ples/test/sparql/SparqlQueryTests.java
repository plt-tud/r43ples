package de.tud.plt.r43ples.test.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;

import de.tud.plt.r43ples.test.examples.CreateExampleGraph;

/**
 * Contains SPARQL queries which will be later used. 
 * 
 * @author Stephan Hensel
 *
 */
public class SparqlQueryTests {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraph.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:8890/sparql";
	/** The user credentials. **/
	private static UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("dba", "dba");
	
	
	/**
	 * Main entry point. Execute some tests.
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void main(String[] args) throws IOException, HttpException {
		
		logger.info("Common revision: \n" + getCommonRevisionWithShortestPath("exampleGraph-revision-1.0-1", "exampleGraph-revision-1.1-1"));
		
		logger.info("Common revision: \n" + getCommonRevisionWithShortestPath("exampleGraph-revision-1", "exampleGraph-revision-1.1-1"));
		
	}
	
	
	/**
	 * Get the common revision of the specified revisions which has the shortest path to the two.
	 * To ensure wise results the revisions should be terminal branch nodes.
	 * 
	 * @param revision1 the first revision should be a terminal branch node
	 * @param revision2 the second revision should be a terminal branch node
	 * @return the nearest common revision
	 * @throws IOException
	 * @throws HttpException 
	 */
	private static String getCommonRevisionWithShortestPath(String revision1, String revision2) throws IOException, HttpException {
		
		logger.info("Get the common revision of <" + revision1 + "> and <" + revision2 + "> which has the shortest path.");
		String query = String.format(
			  "# Query selects the revision which is on both paths (branch 1 and branch 2) and has the minimal path element count \n"
			+ "SELECT ?link MIN(xsd:decimal(?pathElements1) + xsd:decimal(?pathElements2)) AS ?pathElementCountBothBranches \n"
			+ "WHERE { \n"
			+ "	{ \n"
			+ "		# Query creates for each start revision of branch 1 the path element count \n"
			+ "		SELECT ?startRevision1 COUNT(?path1) as ?pathElements1 \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				SELECT ?s ?startRevision1 \n"
			+ "				WHERE { \n"
			+ "					graph <r43ples-revisions> { \n"
			+ "						?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?startRevision1 . \n"
			+ "					} \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTION ( TRANSITIVE, \n"
			+ "					 t_distinct, \n"
			+ "					 t_in(?s), \n"
			+ "					 t_out(?startRevision1), \n"
			+ "					 t_step (?s) as ?link1, \n"
			+ "					 t_step ('path_id') as ?path1, \n"
			+ "					 t_step ('step_no') as ?step1 \n"
			+ "					) . \n"
			+ "			FILTER ( ?s = <%s> ) \n"
			+ "		} GROUP BY ?startRevision1 \n"
			+ "	} \n"
			+ "	OPTIONAL \n"
			+ "	{ \n"
			+ "		# Query creates for each start revision of branch 2 the path element count \n"
			+ "		SELECT ?startRevision2 COUNT(?path2) as ?pathElements2 \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				SELECT ?s ?startRevision2 \n"
			+ "				WHERE { \n"
			+ "					graph <r43ples-revisions> { \n"
			+ "						?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?startRevision2 . \n"
			+ "					} \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTION ( TRANSITIVE, \n"
			+ "					 t_distinct, \n"
			+ "					 t_in(?s), \n"
			+ "					 t_out(?startRevision2), \n"
			+ "					 t_step (?s) as ?link2, \n"
			+ "					 t_step ('path_id') as ?path2, \n"
			+ "					 t_step ('step_no') as ?step1 \n"
			+ "					) . \n"
			+ "			FILTER ( ?s = <%s> ) \n"
			+ "		  } GROUP BY ?startRevision2 \n"
			+ "	} \n"
			+ "	OPTIONAL \n"
			+ "	{ \n"
			+ "		# Query response contains all revisions which are on both paths (branch 1 and branch 2) \n"
			+ "		SELECT DISTINCT ?link1 AS ?link \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				# Query creates all possible paths for branch 1 \n"
			+ "				SELECT ?link1 ?step1 ?path1 \n"
			+ "				WHERE { \n"
			+ "					{ \n"
			+ "						SELECT ?s ?o \n"
			+ "						WHERE { \n"
			+ "							graph <r43ples-revisions> { \n"
			+ "								?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?o . \n"
			+ "							} \n"
			+ "						} \n"
			+ "					} \n"
			+ "					OPTION ( TRANSITIVE, \n"
			+ "							 t_distinct, \n"
			+ "							 t_in(?s), \n"
			+ "							 t_out(?o), \n"
			+ "							 t_step (?s) as ?link1, \n"
			+ "							 t_step ('path_id') as ?path1, \n"
			+ "							 t_step ('step_no') as ?step1 \n"
			+ "							) . \n"
			+ "					FILTER ( ?s = <%s> ) \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTIONAL \n"
			+ "			{ \n"
			+ "				# Query creates all possible paths for branch 2 \n"
			+ "				SELECT ?link2 ?step2 ?path2 \n"
			+ "				WHERE { \n"
			+ "					{ \n"
			+ "						SELECT ?s ?o \n"
			+ "						WHERE { \n"
			+ "							graph <r43ples-revisions> { \n"
			+ "								?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?o . \n"
			+ "							} \n"
			+ "						} \n"
			+ "					} \n"
			+ "					OPTION ( TRANSITIVE, \n"
			+ "							 t_distinct, \n"
			+ "							 t_in(?s), \n"
			+ "							 t_out(?o), \n"
			+ "							 t_step (?s) as ?link2, \n"
			+ "							 t_step ('path_id') as ?path2, \n"
			+ "							 t_step ('step_no') as ?step2 \n"
			+ "							) . \n"
			+ "					FILTER ( ?s = <%s> ) \n"
			+ "				} \n"
			+ "			} \n"
			+ "			FILTER ( ?link1 = ?link2 ) \n"
			+ "		} \n"
			+ "	} \n"
			+ "	FILTER ( ?startRevision1 = ?startRevision2 && ?startRevision1 = ?link ) \n"
			+ "} ORDER BY ?pathElementCountBothBranches \n"
			+ "LIMIT 1", revision1, revision2, revision1, revision2);
		
		String result = executeQueryWithAuthorization(query, "XML");
		
		if (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			logger.info("Common revision found.");
			return qs.getResource("?link").toString();
		}
		
		logger.info("No common revision could be found.");
		return null;		
	}
	
	
	/**
	 * Executes a SPARQL-query against an endpoint without authorization.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorization(String query, String format) throws IOException {
		URL url = null;
		
		url = new URL(endpoint+ "?query=" + URLEncoder.encode(query, "UTF-8") + "&format=" + URLEncoder.encode(format, "UTF-8") + "&timeout=0");
		logger.debug(url.toString());

		URLConnection con = null;
		InputStream in = null;
		con = url.openConnection();
		in = con.getInputStream();
	
		String encoding = con.getContentEncoding();
		encoding = (encoding == null) ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		return body;
		
	}
	
	
	/**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String executeQueryWithAuthorization(String query, String format) throws IOException, HttpException {
		String result = null;
		
		logger.info("Hide all keywords in comments");
		query = query.replace("USER", "#USER").replace("MESSAGE", "#MESSAGE").replace("REVISION", "#REVISION");	
		
		logger.info("Execute query on SPARQL endpoint:\n"+ query);
		DefaultHttpClient httpClient = new DefaultHttpClient();
	    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
			
	    HttpPost request = new HttpPost(endpoint);
		
		//set up HTTP Post Request (look at http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSSparqlProtocol for Protocol)
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("format",format));
		nameValuePairs.add(new BasicNameValuePair("query", query));
		
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		//Execute Query
		HttpResponse response = httpClient.execute(request);
		logger.debug("Statuscode: " + response.getStatusLine().getStatusCode());
		InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
		result = IOUtils.toString(in);
		if (response.getStatusLine().getStatusCode() != Status.OK.getStatusCode()) {
			throw new HttpException(response.getStatusLine().toString()+"\n"+result);
		}
		
		return result;
	}
	
	

}
