package de.tud.plt.r43ples.merging.management;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeQueryTypeEnum;
import de.tud.plt.r43ples.management.ResolutionState;
import de.tud.plt.r43ples.management.SDDTripleStateEnum;
import de.tud.plt.r43ples.management.TripleObjectTypeEnum;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.TreeNode;
import de.tud.plt.r43ples.merging.model.structure.Triple;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;




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
		
		Model model = readTurtleStringToJenaModel(differenceModelToRead);
		
		// Query all difference groups
		String queryDifferenceGroups = prefixes + String.format(
				  "SELECT ?uri ?tripleStateA ?tripleStateB ?automaticResolutionState ?conflicting %n"
				+ "WHERE { %n"
				+ "	?uri a rpo:DifferenceGroup ; %n"
				+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
				+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
				+ "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
				+ "		sddo:isConflicting ?conflicting . %n"
				+ "}");
		logger.debug(queryDifferenceGroups);
		
		// query execution
		QueryExecution qeDifferenceGroups = QueryExecutionFactory.create(queryDifferenceGroups, model);
		ResultSet resultSetDifferenceGroups = qeDifferenceGroups.execSelect();
		// Iterate over all difference groups
	    while(resultSetDifferenceGroups.hasNext()) {
	    	QuerySolution qsDifferenceGroups = resultSetDifferenceGroups.next();
	    	String uri = qsDifferenceGroups.getResource("?uri").toString();
	    	SDDTripleStateEnum tripleStateA = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateA").toString());
	    	SDDTripleStateEnum tripleStateB = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateB").toString());
	    	SDDTripleStateEnum automaticResolutionState = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?automaticResolutionState").toString());
	    	boolean conflicting = qsDifferenceGroups.getLiteral("?conflicting").toString().equals("true^^http://www.w3.org/2001/XMLSchema#boolean");   
	    	
	    	//Wert von conflicting is "true^^http://www.w3.org/2001/XMLSchema#boolean" not "1^^http://www.w3.org/2001/XMLSchema#integer".
	    	//boolean conflicting = qsDifferenceGroups.getLiteral("?conflicting").toString().equals("1^^http://www.w3.org/2001/XMLSchema#integer");   
	    		
	    	logger.info("Original wert von Conflict: "+ qsDifferenceGroups.getLiteral("?conflicting").toString());
	    	logger.info("YXY Test conflicting:" + conflicting );

	    	ResolutionState resolutionState = ResolutionState.DIFFERENCE;
	    	if (conflicting) {
	    		resolutionState = ResolutionState.CONFLICT;
	    	}
	    	
	    	DifferenceGroup differenceGroup = new DifferenceGroup(tripleStateA, tripleStateB, automaticResolutionState, conflicting);
	    	
	    	// Query all differences
			String queryDifferences = prefixes + String.format(
					  "SELECT ?subject ?predicate ?object ?referencedRevisionA ?referencedRevisionB %n"
					+ "WHERE { %n"
					+ "	<%s> a rpo:DifferenceGroup ; %n"
					+ "		rpo:hasDifference ?differenceUri . %n"
					+ "	?differenceUri a rpo:Difference ; %n"
					+ "		rpo:hasTriple ?tripleUri . %n"
					+ "	OPTIONAL { ?differenceUri rpo:referencesA ?referencedRevisionA . } %n"
					+ "	OPTIONAL { ?differenceUri rpo:referencesB ?referencedRevisionB . } %n"
					+ "	?tripleUri rdf:subject ?subject ; %n"
					+ "		rdf:predicate ?predicate ; %n"
					+ "		rdf:object ?object . %n"
					+ "}", uri);
			logger.debug(queryDifferences);
			
			// query execution
			QueryExecution qeDifferences = QueryExecutionFactory.create(queryDifferences, model);
			ResultSet resultSetDifferences = qeDifferences.execSelect();
			// Iterate over all differences
		    while(resultSetDifferences.hasNext()) {
		    	QuerySolution qsDifferences = resultSetDifferences.next();
		    	
		    	String subject = qsDifferences.getResource("?subject").toString();
		    	String predicate = qsDifferences.getResource("?predicate").toString();
	    	
		    	// Differ between literal and resource
				String object = "";
				TripleObjectTypeEnum objectType = null;
				if (qsDifferences.get("?object").isLiteral()) {
					object = qsDifferences.getLiteral("?object").toString();
					objectType = TripleObjectTypeEnum.LITERAL;
				} else {
					object = qsDifferences.getResource("?object").toString();
					objectType = TripleObjectTypeEnum.RESOURCE;
				}
		    	
		    	Triple triple = new Triple(subject, predicate, object, objectType);
		    	
		    	String referencedRevisionA = null;
		    	if (qsDifferences.getResource("?referencedRevisionA") != null) {
		    		referencedRevisionA = qsDifferences.getResource("?referencedRevisionA").toString();
		    	}
		    	String referencedRevisionB = null;
		    	if (qsDifferences.getResource("?referencedRevisionB") != null) {
		    		referencedRevisionB = qsDifferences.getResource("?referencedRevisionB").toString();
		    	}
		    	
		    	// Add further information to difference
		    	// Get the revision number if available
		    	String referencedRevisionLabelA = null;
		    	String referencedRevisionLabelB = null;
		    	
				if ((referencedRevisionA != null) && (referencedRevisionB == null)) {
					String query = prefixes + String.format(
							  "SELECT ?rev %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?rev . %n"
							+ "}", Config.revision_graph, referencedRevisionA);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
					
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelA = qs.getLiteral("?rev").toString();
					}
				} else if ((referencedRevisionA == null) && (referencedRevisionB != null)) {
					String query = prefixes + String.format(
							  "SELECT ?rev %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?rev . %n"
							+ "}", Config.revision_graph, referencedRevisionB);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
					
					// Iterate over all labels
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelB = qs.getLiteral("?rev").toString();
					}
				} else if ((referencedRevisionA != null) && (referencedRevisionB != null)) {
					String query = prefixes + String.format(
							  "SELECT ?revA ?revB %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?revA . %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?revB . %n"
							+ "}", Config.revision_graph, referencedRevisionA, referencedRevisionB);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
					// Iterate over all labels
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelA = qs.getLiteral("?revA").toString();
						referencedRevisionLabelB = qs.getLiteral("?revB").toString();
					}
				}	    	
		    	
		    	Difference difference = new Difference(triple, referencedRevisionA, referencedRevisionLabelA, referencedRevisionB, referencedRevisionLabelB, automaticResolutionState, resolutionState);
		    	differenceGroup.addDifference(tripleToString(triple), difference);
		    	logger.info("yxytree: "+ differenceGroup.getDifferences().entrySet().toString());
		    }
	    	differenceModel.addDifferenceGroup(differenceGroup.getTripleStateA().toString() + "-" + differenceGroup.getTripleStateB().toString(), differenceGroup);
	    }
	    
	    logger.info("Difference model successfully read.");	
		logger.info("DT"+differenceModel.getDifferenceGroups().toString());
		logger.info("Unittest:"+differenceModel.getDifferenceGroups().get("ADDED-DELETED").isConflicting());
	}
	
	
	
	
	
	//copy Hensel
	public static Model readTurtleStringToJenaModel(String triples) throws IOException {
		Model model = null;
		model = ModelFactory.createDefaultModel();
		InputStream is = new ByteArrayInputStream(triples.getBytes());
		model.read(is, null, "TURTLE");
		is.close();
		
		return model;
	}
	
	//copy Hensel
	public static SDDTripleStateEnum convertSDDStringToSDDTripleState(String state) {
		if (state.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
			return SDDTripleStateEnum.ADDED;
		} else if (state.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
			return SDDTripleStateEnum.DELETED;
		} else if (state.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
			return SDDTripleStateEnum.ORIGINAL;
		} else if (state.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
			return SDDTripleStateEnum.NOTINCLUDED;
		} else {
			return null;
		}
	}
	
	//copy Hensel
	public static String tripleToString(Triple triple) {
		if (triple.getObjectType().equals(TripleObjectTypeEnum.LITERAL)) {
			logger.debug(String.format("<%s> %s \"%s\" .", triple.getSubject(), getPredicate(triple), triple.getObject()));
			
			logger.info("LITERAL Triple");
			return String.format("<%s> %s \"%s\" .", triple.getSubject(), getPredicate(triple), triple.getObject());
			
		} else {
			logger.debug(String.format("<%s> %s <%s> .", triple.getSubject(), getPredicate(triple), triple.getObject()));
			logger.info("Ohne LITERAL Triple");

			return String.format("<%s> %s <%s> .", triple.getSubject(), getPredicate(triple), triple.getObject());
		}
	}
	
	public static String getSubject(Triple triple) {
		return "<" + triple.getSubject() + ">";
	}
	
	
	public static String getPredicate(Triple triple) {
		if (triple.getPredicate().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
			return "a";
		} else {
			return "<" + triple.getPredicate() + ">";
		}
	}
	
	public static String getObject(Triple triple) {
		if (triple.getObjectType().equals(TripleObjectTypeEnum.LITERAL)) {
			if (triple.getObject().contains("@")) {
				return "\"" + triple.getObject().substring(0, triple.getObject().lastIndexOf("@")) + "\"@" + triple.getObject().substring(triple.getObject().lastIndexOf("@") + 1, triple.getObject().length());
			} else {
				return "\"" + triple.getObject() + "\"";
			}
		} else {
			return "<" + triple.getObject() + ">";		
		}
	}
	
	
	public static void createDifferenceTree(DifferenceModel differenceModel, List<TreeNode> treeList) throws ConfigurationException{
//		List<TreeNode> treeList = new ArrayList<TreeNode>();
		treeList.clear();	
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			//get Triple List von jede DifferenceGroup
			List<String> tripleList = new ArrayList<String>();
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			
			//get Name von differenceGroup
			String groupName = (String) entryDG.getKey();
			
			logger.info("Tree test groupName: "+ groupName);
			//get jede differenceGroup
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			
			//get conflict status von differenceGroup
			boolean status = differ.isConflicting();
			
			logger.info("Tree test Status: "+ status);

			//get String name von jede Triples
			Iterator<Entry<String, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				Entry<String, Difference> entryDF = iterDIF.next();
				// get jede tripleName
				String tripleName = entryDF.getKey();
				logger.info("Tree test TripleName: "+ tripleName);
				
				Triple triple = entryDF.getValue().getTriple();
				
				String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(triple));
				String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
				String object = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(triple));
				
				StringBuilder TripleBuilder = new StringBuilder();
				String prefixTriple = TripleBuilder.append(subject).append(" ").append(predicate).append(" ").append(object).toString();
				
				logger.info("Tree test PrefixTriple: "+ prefixTriple);

				//get alle tripleNames in der DifferenceGroup
				tripleList.add(prefixTriple);
			}
			// create treeNode
			TreeNode treeNode = new TreeNode(groupName, tripleList, status);
			treeList.add(treeNode);
		}
		
		logger.info("Difference Tree successful created.");
	}
	
	
	/**
	 * Converts a triple string to a string in which URIs are replaced by prefixes which were specified in the configuration.
	 * If no prefix was found or if input string is a literal the input string will be returned.
	 * 
	 * @param tripleString the triple string (subject or predicate or object) to convert
	 * @return the converted triple string or input string
	 * @throws ConfigurationException 
	 */
	public static String convertTripleStringToPrefixTripleString(String tripleString) throws ConfigurationException {
		//read clent.conf to get the prefix map
		ClientConfig.readConfig("client.conf");
		logger.info("Prefix Mapping geted: "+ClientConfig.prefixMappings.entrySet().toString());
		
		if (tripleString.contains("<") && tripleString.contains(">")) {
			String tripleStringConverted = tripleString.trim().replaceAll("<", "").replaceAll(">", "");
			int lastIndexSlash = tripleStringConverted.lastIndexOf("/");
			int lastIndexHash = tripleStringConverted.lastIndexOf("#");
			if ((lastIndexSlash == -1) && (lastIndexHash == -1)) {
				return tripleString;
			} else {
				int index = 0;
				if (lastIndexSlash > lastIndexHash) {
					// Slash separator found
					index = lastIndexSlash + 1;
				} else {
					// Hash separator found
					index = lastIndexHash + 1;
				}
				String subString = tripleStringConverted.substring(0, index);
				
				logger.info("get substring: "+subString);
				// Try to find the prefix
				if (ClientConfig.prefixMappings.containsKey(subString)) {
					return ClientConfig.prefixMappings.get(subString) + ":" + tripleStringConverted.substring(index, tripleStringConverted.length());
				}
			}
		}
		return tripleString;
	}

	
}



















