package de.tud.plt.r43ples.visualisation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;


public class RdfUmlDiagramExport {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(RdfUmlDiagramExport.class);
	


	public static boolean generateRdfUmlDiagram(String graph, String fileName) throws AuthenticationException, IOException{
		// download revision graph to a file and pass it to rdfUmlDiagram.py python script
		String filePath = Config.visualisation_path +  fileName;
		logger.debug(filePath);
		
		String revInformation = RevisionManagement.getRevisionInformation(graph, "text/turtle");
		FileUtils.writeStringToFile(new File(filePath), revInformation);
		
		// FIXME: execute python script
		
		return true;
	}
}
