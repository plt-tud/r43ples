package de.tud.plt.r43ples.merging.management;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.MergeQueryTypeEnum;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;




public class ProcessManagement {
	/** The logger. */
	private static Logger logger = Logger.getLogger(ProcessManagement.class);
	/** The SPARQL prefixes. **/
	private static final String prefixes = 
			  "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rpo: <http://eatld.et.tu-dresden.de/rpo#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";

	
	public static void startNewMergeProcess(){
	}
	
	public static String createMergeQuery(String graphName, String sdd, String user, String commitMessage, MergeQueryTypeEnum type, String branchNameA, String branchNameB, String triples) throws IOException {
		logger.info("Execute merge query of type " + type.toString());
		String query = "";
		
		String queryTemplateCommon = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateWith = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		
		String queryTemplateAuto = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateManual = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		if (type.equals(MergeQueryTypeEnum.COMMON)) {
			query = String.format(queryTemplateCommon, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(MergeQueryTypeEnum.WITH)) {
			query = String.format(queryTemplateWith, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		} else if (type.equals(MergeQueryTypeEnum.AUTO)) {
			query = String.format(queryTemplateAuto, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(MergeQueryTypeEnum.MANUAL)) {
			query = String.format(queryTemplateManual, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		}
		
		return query;

	}
	
	public static void readDifferenceModel(String differenceModelToRead, DifferenceModel differenceModel) throws IOException {
		logger.info("Start reading difference model.");
		differenceModel.clear();
		
	}
}




















