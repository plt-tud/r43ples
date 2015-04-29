package de.tud.plt.r43ples.merging.control;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.management.BranchManagement;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;

import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response;


public class MergingControl {
	private static Logger logger = Logger.getLogger(MergingControl.class);
	private static DifferenceModel differenceModel = new DifferenceModel();

	
	public static String getHtmlOutput(String optradio) {
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/mergingView.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("optradio", optradio);	    
	    mustache.execute(sw, scope);		
		return sw.toString();
	}
	
	public static String getMenuHtmlOutput() {
		List<String> graphList = RevisionManagement.getRevisedGraphs();
	
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merging.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("merging_active", true);
		scope.put("graphList", graphList);
		
	    mustache.execute(sw, scope);		
		return sw.toString();
	}	
	
	public static String getBranchInformation(String graph) throws IOException {
		List<String> branchList = BranchManagement.getAllBranchNamesOfGraph(graph);
		StringBuilder branchInformation = new StringBuilder();
		for(String branchName:branchList){
			branchInformation.append("<option value="+"\""+branchName+"\""+">"+branchName+"</option>");
		}
		System.out.println("branch success created");
		return branchInformation.toString();
	}
	
	public static void getMergeProcess(Response response) throws IOException{
		//ob diese satz richt ist oder nicht?
		if (response.getStatusInfo() == Response.Status.CONFLICT){
			logger.info("Merge query produced conflicts.");
			ProcessManagement.readDifferenceModel(response.getEntity().toString(), differenceModel);
		} else if (response.getStatusInfo() == Response.Status.CREATED){
			logger.info("Merge query produced no conflicts. Merged revision was created.");
			
		} else {
			// error occurred
		}		
		
	}
}











