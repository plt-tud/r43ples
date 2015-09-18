package de.tud.plt.r43ples.merging.control;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.merging.model.structure.CommitModel;

public class FastForwardControl {
	private CommitModel commitModel;
	
	/**
	 * get the report page of fast forward query
	 * @param graphName
	 * */

	public String getFastForwardReportView(String graphName) {
		Map<String, Object> scope = new HashMap<String, Object>();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/merge/mergingResultView.mustache");
		StringWriter sw = new StringWriter();		
		scope.put("graphName", graphName);
		scope.put("commit", commitModel);
		scope.put("merging_active", true);
		
		mustache.execute(sw, scope);	
		return sw.toString();	
	}
	
	/**check the condition for fast forward strategy
	 * @param graphName : name of named graph
	 * @param branch1 : name of branch1
	 * @param branch2 : name of branch2
	 * @throws InternalErrorException */
	
	public static boolean fastForwardCheck(String graphName, String branch1 , String branch2) throws InternalErrorException {
		if(branch1.equals(branch2)) {
			return false;
		}
		
		//get last revision of each branch
		String revisionUriA = RevisionManagement.getRevisionUri(graphName, branch1);
		String revisionUriB = RevisionManagement.getRevisionUri(graphName, branch2);
		
		return StrategyManagement.isFastForward(revisionUriA, revisionUriB);
	}
	
	
	public void createCommitModel(String graphName, String sddName, String user, String message, String branch1, String branch2, String strategy, String type){
		commitModel = new CommitModel(graphName, sddName, user, message, branch1, branch2, strategy, type);
	}
	
}
