package de.tud.plt.r43ples.merging.model.structure;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**store the information of merge in commit model
 * 
 * @author Xinyu Yang
 * 
 * */

public class CommitModel {
	String graphName = null;
	String sddName = null;
	String user = null;
	String message = null;
	String branch1 = null;
	String branch2 = null;
	String strategy = null;
	String type = null;
	
	public CommitModel(String graphName, String sddName, String user,
			String message, String branch1, String branch2, String strategy, String type) {
		this.graphName = graphName;
		this.sddName = sddName;
		this.user = user;
		this.message = message;
		this.branch1 = branch1;
		this.branch2 = branch2;
		this.strategy = strategy;
		this.type = type;
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	public String getSddName() {
		return sddName;
	}

	public void setSddName(String sddName) {
		this.sddName = sddName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getBranch1() {
		return branch1;
	}

	public void setBranch1(String branch1) {
		this.branch1 = branch1;
	}

	public String getBranch2() {
		return branch2;
	}

	public void setBranch2(String branch2) {
		this.branch2 = branch2;
	}
	
	public String getStrategy(){
		return strategy;
	}
	
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
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


