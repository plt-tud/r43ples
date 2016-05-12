package de.tud.plt.r43ples.merging.model.structure;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.RevisionManagement;

/**store the information of merge in commit model
 * 
 * @author Markus Graube
 * @author Xinyu Yang
 * 
 * */

public class MergeCommitModel {
	String graphName = null;
	String sddName = null;
	String user = null;
	String message = null;
	String branch1 = null;
	String branch2 = null;
	String strategy = null;
	String type = null;
	String oldRevisionGraph = null;
	
	public MergeCommitModel(String graphName, String sddName, String user,
			String message, String branch1, String branch2, String strategy, String type) {
		this.graphName = graphName;
		this.sddName = sddName;
		this.user = user;
		this.message = message;
		this.branch1 = branch1;
		this.branch2 = branch2;
		this.strategy = strategy;
		this.type = type;
		this.oldRevisionGraph = RevisionManagement.getRevisionInformation(graphName, "application/json");
	}

	public String getGraphName() {
		return graphName;
	}


	public String getSddName() {
		return sddName;
	}


	public String getUser() {
		return user;
	}

	public String getMessage() {
		return message;
	}

	public String getBranch1() {
		return branch1;
	}


	public String getBranch2() {
		return branch2;
	}

	
	public String getStrategy(){
		return strategy;
	}
	
	public String getType(){
		return type;
	}
	
	public String getReportView() {
		Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		scope.put("commit", this);
		scope.put("merging_active", true);
		
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merge_report.mustache");
	    mustache.execute(sw, scope);		
		return sw.toString();	
	}
}


