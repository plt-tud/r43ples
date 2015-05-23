package de.tud.plt.r43ples.merging.control;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.management.BranchManagement;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeModel;
import de.tud.plt.r43ples.merging.model.structure.IndividualModel;
import de.tud.plt.r43ples.merging.model.structure.TableModel;
import de.tud.plt.r43ples.merging.model.structure.TreeNode;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import javax.ws.rs.core.Response;


public class MergingControl {
	private static Logger logger = Logger.getLogger(MergingControl.class);
	private static DifferenceModel differenceModel = new DifferenceModel();
	private static List<TreeNode> treeList = new ArrayList<TreeNode>();
	private static TableModel tableModel = new TableModel();
	private static HighLevelChangeModel highLevelChangeModel = new HighLevelChangeModel();
	
	/** The individual model of branch A. **/
	private static IndividualModel individualModelBranchA;
	/** The individual model of branch B. **/
	private static IndividualModel individualModelBranchB;	
	/** The properties array list. **/
	private static ArrayList<String> propertyList;

	
	public static String getHtmlOutput(String graphName) {
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/mergingView.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("graphName", graphName);	    
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
	
	public static String getViewHtmlOutput(String graphName) throws TemplateException, IOException {	
		Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		freemarker.template.Template temp = null; 
		String name = "mergingView3.ftl";
		try {  
            // 通过Freemarker的Configuration读取相应的Ftl  
            Configuration cfg = new Configuration();  
            // 设定去哪里读取相应的ftl模板  
            cfg.setClassForTemplateLoading(MergingControl.class, "/templates");
            // 在模板文件目录中寻找名称为name的模板文件  
            temp = cfg.getTemplate(name);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
		
		/**conList fuer conflict triple
		 * diffList fuer deference triple*/
		List<TreeNode> conList = new ArrayList<TreeNode>();
		List<TreeNode> diffList = new ArrayList<TreeNode>();
		Iterator<TreeNode> itG = treeList.iterator();
	 	
		/**create conList and diffList*/
	 	String conStatus = "0";
	 	while(itG.hasNext()){
	 		TreeNode node = itG.next();
	 		
	 		if(node.status == true){
	 			conStatus = "1";
		 		conList.add(node);
	 		}else{
	 			diffList.add(node);
	 		}	 		
	 	}
	 	
	 	scope.put("tableRowList", tableModel.getTripleRowList());
	 	scope.put("graphName", graphName);	
		scope.put("conList",conList);
		scope.put("diffList",diffList);
		scope.put("conStatus", conStatus);
		
		temp.process(scope,sw);		
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
	
	public static void getMergeProcess(Response response, String graphName, String branchNameA, String branchNameB) throws IOException, ConfigurationException, InternalErrorException{
		//ob diese satz richt ist oder nicht?
		if (response.getStatusInfo() == Response.Status.CONFLICT){
			logger.info("Merge query produced conflicts.");
			
			ProcessManagement.readDifferenceModel(response.getEntity().toString(), differenceModel);
			
			
			ProcessManagement.createDifferenceTree(differenceModel, treeList);
			
			ProcessManagement.createTableModel(differenceModel, tableModel);
			
			ProcessManagement.createHighLevelChangeRenamingModel(highLevelChangeModel, differenceModel);
			
			// Create the individual models of both branches
			individualModelBranchA = ProcessManagement.createIndividualModelOfRevision(graphName, branchNameA, differenceModel);
			logger.info("Individual Model A Test : " + individualModelBranchA.getIndividualStructures().keySet().toString());

			individualModelBranchB = ProcessManagement.createIndividualModelOfRevision(graphName, branchNameB, differenceModel);
			logger.info("Individual Model B Test : " + individualModelBranchB.getIndividualStructures().keySet().toString());

			
			
			// Create the property list of revisions
			propertyList = ProcessManagement.getPropertiesOfRevision(graphName, branchNameA, branchNameB);
			
			Iterator<String> pit = propertyList.iterator();
			while(pit.hasNext()){
				logger.info("propertyList Test : " + pit.next().toString());
			}
			
			
			
		} else if (response.getStatusInfo() == Response.Status.CREATED){
			logger.info("Merge query produced no conflicts. Merged revision was created.");
			
		} else {
			// error occurred
		}		
		
	}
	
	public static String getIndividualView(String individual) throws TemplateException, IOException{
		Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		freemarker.template.Template temp = null; 
		String name = "individualView.ftl";
		try {  
            // 通过Freemarker的Configuration读取相应的Ftl  
            Configuration cfg = new Configuration();  
            // 设定去哪里读取相应的ftl模板  
            cfg.setClassForTemplateLoading(MergingControl.class, "/templates");
            // 在模板文件目录中寻找名称为name的模板文件  
            temp = cfg.getTemplate(name);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
		
	 	
		scope.put("individual", individual);
		
		temp.process(scope,sw);		
		return sw.toString();	
		
	}
}











