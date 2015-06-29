package de.tud.plt.r43ples.merging.control;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.merging.model.structure.CommitModel;
import de.tud.plt.r43ples.webservice.Endpoint;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FastForwardControl {
	private static Logger logger = Logger.getLogger(FastForwardControl.class);
	private static CommitModel commitModel;
	
	/**get the report page of fast forward query
	 * @throws IOException 
	 * @throws TemplateException */

	public static String getFastForwardReportView(String graphName) throws TemplateException, IOException{
		Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		freemarker.template.Template temp = null; 
		String name = "fastForwardReportView.ftl";
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
		
		scope.put("graphName", graphName);
		scope.put("commit", commitModel);
		scope.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		scope.put("git", GitRepositoryState.getGitRepositoryState());
		
		temp.process(scope,sw);		
		return sw.toString();	
	}
	
	public static void createCommitModel(String graphName, String sddName, String user, String message, String branch1, String branch2, String strategy){
		commitModel = new CommitModel(graphName, sddName, user, message, branch1, branch2, strategy);
	}
	
}
