package de.tud.plt.r43ples.adminInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;


/**
 * Executer of diff and resolve tool. Provides command line interface.
 * 
 * @author Stephan Hensel
 *
 */
public class Executer {
	
	private HashMap<String, Differences> checkedForDifferencesResult = null;
	private DiffResolveTool diffResolveTool = null;
	private Model newGraph = null;
	private Model oldGraph = null;
	
	private boolean exitFlag = false;

	
	/**
	 * The constructor.
	 * 
	 * @param checkedForDifferencesResult
	 * @param diffResolveTool
	 * @param newGraph
	 * @param oldGraph
	 */
	public Executer(HashMap<String, Differences> checkedForDifferencesResult, DiffResolveTool diffResolveTool, Model newGraph, Model oldGraph) {
		this.checkedForDifferencesResult = checkedForDifferencesResult;
		this.diffResolveTool = diffResolveTool;
		this.newGraph = newGraph;
		this.oldGraph = oldGraph;
	}
	
	
	/**
	 * Get the merged graph 1.
	 * 
	 * @return the merged graph
	 */
	public Model getMergedGraph1() {
		return newGraph;
	}
	
	/**
	 * Get the merged graph 2.
	 * 
	 * @return the merged graph
	 */
	public Model getMergedGraph2() {
		return oldGraph;
	}
	
		
	/**
	 * Reads data from System.in.
	 * 
	 * @param toWrite the message to write
	 * @param min the minimal value
	 * @param max the maximal value
	 * @return entered value
	 */
	private Integer readDataFromSystemIN(String toWrite, Integer min, Integer max) {
		System.out.print(toWrite);
		
		String inputData = "";
		int num_std;
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

		try {
			inputData=br.readLine();
			//go back to list with all elements
			if (inputData.equals("b")) {
				System.out.println("Going back to element list!\n");
				printListOfAllElements();
				return -1;
			} else if (inputData.equals("c")) {
				System.out.println("Close merging!\n");
				return -2;
			} else {
				num_std=Integer.parseInt(inputData);
				if ((num_std < min) || (num_std > max)) {
					System.out.println("The ID '" + num_std + "' is not a valid ID!");
					return readDataFromSystemIN(toWrite, min, max);
				}
				System.out.println();
				return num_std;
			}
		} catch (NumberFormatException e) {
			System.out.println("There was an error while parsing '" + inputData + "'! Is it a valid integer value?");
			return readDataFromSystemIN(toWrite, min, max);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
		
	}
	
	
	/**
	 * Reads command data from System.in.
	 * 
	 * @param toWrite the message to write
	 * @return entered value
	 */
	private String readCommandDataFromSystemIN(String toWrite) {
		System.out.print(toWrite);
		
		String inputData = "";
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

		try {
			inputData=br.readLine();
			if (inputData.equals("a")) {
				System.out.println();
				return "Added";
			} else if (inputData.equals("r")) {
				System.out.println();
				return "Removed";
			} else if (inputData.equals("s")) {
				System.out.println();
				return "Same";
			} else if (inputData.equals("w")) {
				System.out.println();
				return "accept";
			} else if (inputData.equals("d")) {
				System.out.println();
				return "restore";
			} else if (inputData.equals("b")) {
				System.out.println("Going back to element list!\n");
				printListOfAllElements();
				return "";
			} else {
				System.out.println("Input value '" + inputData + "' was not a valid command!");
				return readCommandDataFromSystemIN(toWrite);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputData;
		
	}
	
	
	/**
	 * Prints list of all elements.
	 * 
	 */
	public void printListOfAllElements() {
		System.out.println("============================================================\n");
		System.out.println("List of all elements:\n");
			
		//update view
		checkedForDifferencesResult = diffResolveTool.checkForDifferences(newGraph, oldGraph);
		
		//output all differences
		
		//generate column width
		int idWidth = Integer.toString(countDifferencesElement(checkedForDifferencesResult)).length();
		if (idWidth <= 2) {
			idWidth = 2;
		}
		idWidth = 4 + idWidth - 2 + 1;
		int elementWidth = getMaxStringLength(checkedForDifferencesResult.keySet());
		if (elementWidth <= 8) {
			elementWidth = 8;
		}
		elementWidth = idWidth + 10 + elementWidth - 8 + 1;
		
		int width = elementWidth + 1 + 13;
		
		//string to print
		String hLine = "";
		String headerLine = "";
		//save id and element
		HashMap<Integer, String> elementMap = new HashMap<Integer, String>();
		
		for (int i=0; i<=width; i++) {
			if ((i == 0) || (i == idWidth) || (i == elementWidth) || (i == width)) {
				hLine = hLine + "+";
				headerLine = headerLine + "|";
			} else {
				hLine = hLine + "-";
				
				if (i == 2) {
					headerLine = headerLine + "ID";
				} else if (i == idWidth + 2) {
					headerLine = headerLine + "elements";
				} else if (i == elementWidth + 2) {
					headerLine = headerLine + "difference";
				} else {
					if (headerLine.length() == i) {
						headerLine = headerLine + " ";
					}
				}
			}
		}
		
		System.out.println(hLine);
		System.out.println(headerLine);
		System.out.println(hLine);
		
		String entryLine = "";
		int countID = 0;
		
		Iterator<String> elementIte = checkedForDifferencesResult.keySet().iterator();
		while(elementIte.hasNext()) {
			String nextElement = elementIte.next();
			for (int i=0; i<=width; i++) {
				if ((i == 0) || (i == idWidth) || (i == elementWidth) || (i == width)) {
					entryLine = entryLine + "|";
				} else {
					
					boolean differenceBool = diffResolveTool.differenceInElement(nextElement, checkedForDifferencesResult);
					
					if (i == 2) {
						if (differenceBool == true) {
							entryLine = entryLine + countID;
							elementMap.put(Integer.valueOf(countID), nextElement);
							countID++;
						} else {
							entryLine = entryLine + " ";
						}
					} else if (i == idWidth + 2) {
						entryLine = entryLine + nextElement;
					} else if (i == elementWidth + 2) {
						entryLine = entryLine + differenceBool;
					} else {
						if (entryLine.length() == i) {
							entryLine = entryLine + " ";
						}
					}
				}
			}
			System.out.println(entryLine);
			entryLine = "";
		}
		
		System.out.println(hLine);
		System.out.println();
		
		Integer readValue = readDataFromSystemIN("Enter ID to view detail information or c to exit: ", 0 , countID - 1);
		if ((readValue == -2) || (exitFlag)) {
			exitFlag = true;
			return;
		}
		
		String commandValue = readCommandDataFromSystemIN("Command options:\n a - to show added specific elements\n r - to show removed specific elements\n s - to show all specific elements which are in both graphs\n b - to go back\nEnter command: ");
		
		System.out.println("============================================================\n");
		
		switch (commandValue) {
		case "Added":
			printAddedRemoved(elementMap.get(readValue), commandValue);
			break;
		case "Removed":
			printAddedRemoved(elementMap.get(readValue), commandValue);
			break;
		case "Same":
			printSame(elementMap.get(readValue));
			break;
		default:
			break;
		}
		
		
	}
	
	
	/**
	 * Prints all added or removed data.
	 * 
	 * @param element the element
	 * @param kind the kind of data to print (Added or Removed)
	 */
	private void printAddedRemoved(String element, String kind) {
		
		System.out.println("Differences of element '" + element + "':\n");
		
		//output all differences
		
		System.out.println(kind + ":");
		
		ArrayList<Node> list = null;
		
		switch (kind) {
		case "Added":
			list = checkedForDifferencesResult.get(element).getAdded();
			break;
		case "Removed":
			list = checkedForDifferencesResult.get(element).getRemoved();
			break;
		default:
			break;
		}
		
		
		//generate column width
		int idWidth = Integer.toString(list.size()).length();
		if (idWidth <= 2) {
			idWidth = 2;
		}
		idWidth = 4 + idWidth - 2 + 1;
		int elementWidth = getMaxNodeStringLength(list);
		if (elementWidth <= 16) {
			elementWidth = 16;
		}
		elementWidth = idWidth + 18 + elementWidth - 16 + 1;
		
		//string to print
		String hLine = "";
		String headerLine = "";
		//save id and element
		HashMap<Integer, Node> elementMap = new HashMap<Integer, Node>();
		
		for (int i=0; i<=elementWidth; i++) {
			if ((i == 0) || (i == idWidth) || (i == elementWidth)) {
				hLine = hLine + "+";
				headerLine = headerLine + "|";
			} else {
				hLine = hLine + "-";
				
				if (i == 2) {
					headerLine = headerLine + "ID";
				} else if (i == idWidth + 2) {
					headerLine = headerLine + "specific element";
				} else {
					if (headerLine.length() == i) {
						headerLine = headerLine + " ";
					}
				}
			}
		}
		
		System.out.println(hLine);
		System.out.println(headerLine);
		System.out.println(hLine);
		
		String entryLine = "";
		int countID = 0;
		
		Iterator<Node> elementIte = list.iterator();
		while(elementIte.hasNext()) {
			Node nextNode = elementIte.next();
			String nextElement = nextNode.toString();
			
			for (int i=0; i<=elementWidth; i++) {
				if ((i == 0) || (i == idWidth) || (i == elementWidth)) {
					entryLine = entryLine + "|";
				} else {
					
					if (i == 2) {
						entryLine = entryLine + countID;
						elementMap.put(Integer.valueOf(countID), nextNode);
						countID++;
					} else if (i == idWidth + 2) {
						entryLine = entryLine + nextElement;
					} else {
						if (entryLine.length() == i) {
							entryLine = entryLine + " ";
						}
					}
				}
			}
			System.out.println(entryLine);
			entryLine = "";
		}
		
		System.out.println(hLine);
		System.out.println();
		
		Integer readValue = readDataFromSystemIN("Enter ID to view detail information/b to go back: ", 0 , countID - 1);
		
		System.out.println("============================================================\n");
		
		viewDetailDataAddedRemoved(element, elementMap.get(readValue), kind);
		
		
	}
	

	/**
	 * View detail data (added/removed).
	 * 
	 * @param element the element
	 * @param node the special element node
	 * @param kind the kind of data to print (Added or Removed)
	 */
	private void viewDetailDataAddedRemoved(String element, Node node, String kind) {
		
		System.out.println("Detail data of " + node.toString() + ": ");
		
		Model graph = null;
		switch (kind) {
		case "Added":
			graph = newGraph;
			break;
		case "Removed":
			graph = oldGraph;
			break;
		default:
			break;
		}
		StructureClass sElement = diffResolveTool.getStructure().getClasses().get(element);
		
		HashMap<String, ArrayList<Node>> subElementsMap = new HashMap<String, ArrayList<Node>>();
		
		Iterator<String> subElementsIte = sElement.getProperties().iterator();
    	while (subElementsIte.hasNext()) {
    		String subElementsName = subElementsIte.next();
    		
    		//create query
    		String queryStringSame =	"SELECT ?o " +
    									"WHERE {<" + node.toString() + "> <" + subElementsName + "> ?o }";

    		Query querySame = QueryFactory.create(queryStringSame);
    		
    		//query execution on graph
			QueryExecution qeSame = QueryExecutionFactory.create(querySame, graph);
			
			ResultSet resultsSame = qeSame.execSelect();
			ArrayList<Node> name = new ArrayList<Node>();
			
			//get all sub element attributes
			while(resultsSame.hasNext()) {
				QuerySolution qsSame = resultsSame.next();
				
					
				// get name						
				try {
					name.add(NodeFactory.createLiteral(qsSame.getLiteral("?o").getValue().toString()));
				} catch (ClassCastException e) {
					name.add(NodeFactory.createURI(qsSame.getResource("?o").toString()));
				}
				
				
			}
			subElementsMap.put(subElementsName, name);
    	}
    	
    	//generate column width
		int idWidth = getMaxStringLength(subElementsMap.keySet());
		if (idWidth <= 4) {
			idWidth = 4;
		}
		idWidth = 6 + idWidth - 4 + 1;
		int elementWidth = getMaxNodeMapLength(subElementsMap);
		if (elementWidth <= 5) {
			elementWidth = 5;
		}
		elementWidth = idWidth + 7 + elementWidth - 5 + 1;
		
		//string to print
		String hLine = "";
		String headerLine = "";
		
		for (int i=0; i<=elementWidth; i++) {
			if ((i == 0) || (i == idWidth) || (i == elementWidth)) {
				hLine = hLine + "+";
				headerLine = headerLine + "|";
			} else {
				hLine = hLine + "-";
				
				if (i == 2) {
					headerLine = headerLine + "name";
				} else if (i == idWidth + 2) {
					headerLine = headerLine + "value";
				} else {
					if (headerLine.length() == i) {
						headerLine = headerLine + " ";
					}
				}
			}
		}
		
		System.out.println(hLine);
		System.out.println(headerLine);
		System.out.println(hLine);
		
		String entryLine = "";
		
		Iterator<String> elementIte = subElementsMap.keySet().iterator();
		while(elementIte.hasNext()) {
			String nextString = elementIte.next();
			
			Iterator<Node> nodeIte = subElementsMap.get(nextString).iterator();
			while (nodeIte.hasNext()) {
				Node nextNode = nodeIte.next();
				
				for (int i=0; i<=elementWidth; i++) {
					if ((i == 0) || (i == idWidth) || (i == elementWidth)) {
						entryLine = entryLine + "|";
					} else {
						
						if (i == 2) {
							entryLine = entryLine + nextString;
							nextString = " ";
						} else if (i == idWidth + 2) {
							entryLine = entryLine + nextNode.toString();
						} else {
							if (entryLine.length() == i) {
								entryLine = entryLine + " ";
							}
						}
					}
				}
				
				System.out.println(entryLine);
				entryLine = "";
				
			}

		}
		
		System.out.println(hLine);
		System.out.println();
		
		String readCommandValue = readCommandDataFromSystemIN("Enter w to accept/d to restore/b to go back: ");
		
		HashMap<String, Model> resultGraphMap = new HashMap<String, Model>();
		
		if (kind.equals("Added")) {
			switch (readCommandValue) {
			case "accept":
				resultGraphMap = diffResolveTool.acceptAdded(element, node, newGraph, oldGraph, true);
				newGraph = resultGraphMap.get("new_model");
				oldGraph = resultGraphMap.get("old_model");		
				break;
			case "restore":
				resultGraphMap = diffResolveTool.acceptAdded(element, node, newGraph, oldGraph, false);
				newGraph = resultGraphMap.get("new_model");
				oldGraph = resultGraphMap.get("old_model");	
				break;
			default:
				break;
			}
		} else if (kind.equals("Removed")) {
			switch (readCommandValue) {
			case "accept":
				resultGraphMap = diffResolveTool.acceptRemoved(element, node, newGraph, oldGraph, true);
				newGraph = resultGraphMap.get("new_model");
				oldGraph = resultGraphMap.get("old_model");		
				break;
			case "restore":
				resultGraphMap = diffResolveTool.acceptRemoved(element, node, newGraph, oldGraph, false);
				newGraph = resultGraphMap.get("new_model");
				oldGraph = resultGraphMap.get("old_model");	
				break;
			default:
				break;
			}
		}
		
				
		System.out.println("============================================================\n");
		
		printListOfAllElements();
    	
    	
	}
	
	
	/**
	 * Prints all same data.
	 * 
	 * @param element the element
	 */
	private void printSame(String element) {
		
		System.out.println("Differences of element '" + element + "':\n");
		
		//output all differences
		
		System.out.println("Same:");
		
		ArrayList<Node> list = checkedForDifferencesResult.get(element).getSame();

		//generate column width
		int idWidth = Integer.toString(list.size()).length();
		if (idWidth <= 2) {
			idWidth = 2;
		}
		idWidth = 4 + idWidth - 2 + 1;
		int elementWidth = getMaxNodeStringLength(list);
		if (elementWidth <= 16) {
			elementWidth = 16;
		}
		elementWidth = idWidth + 18 + elementWidth - 16 + 1;
		
		int width = elementWidth + 1 + 13;
		
		//string to print
		String hLine = "";
		String headerLine = "";
		//save id and element
		HashMap<Integer, Node> elementMap = new HashMap<Integer, Node>();
		
		for (int i=0; i<=width; i++) {
			if ((i == 0) || (i == idWidth) || (i == elementWidth) || (i == width)) {
				hLine = hLine + "+";
				headerLine = headerLine + "|";
			} else {
				hLine = hLine + "-";
				
				if (i == 2) {
					headerLine = headerLine + "ID";
				} else if (i == idWidth + 2) {
					headerLine = headerLine + "specific element";
				} else if (i == elementWidth + 2) {
					headerLine = headerLine + "difference";
				} else {
					if (headerLine.length() == i) {
						headerLine = headerLine + " ";
					}
				}
			}
		}
		
		System.out.println(hLine);
		System.out.println(headerLine);
		System.out.println(hLine);
		
		String entryLine = "";
		int countID = 0;
		
		Iterator<Node> elementIte = list.iterator();
		while(elementIte.hasNext()) {
			Node nextNode = elementIte.next();
			String nextElement = nextNode.toString();
			
			for (int i=0; i<=width; i++) {
				if ((i == 0) || (i == idWidth) || (i == elementWidth) || (i == width)) {
					entryLine = entryLine + "|";
				} else {
					
					boolean differenceBool = diffResolveTool.differenceInSpecificElement(element, nextNode, checkedForDifferencesResult);
					
					if (i == 2) {
						if (differenceBool == true) {
							entryLine = entryLine + countID;
							elementMap.put(Integer.valueOf(countID), nextNode);
							countID++;
						} else {
							entryLine = entryLine + " ";
						}
					} else if (i == idWidth + 2) {
						entryLine = entryLine + nextElement;
					} else if (i == elementWidth + 2) {
						entryLine = entryLine + differenceBool;
					} else {
						if (entryLine.length() == i) {
							entryLine = entryLine + " ";
						}
					}
				}
			}
			System.out.println(entryLine);
			entryLine = "";
		}
		
		System.out.println(hLine);
		System.out.println();
		
		Integer readValue = readDataFromSystemIN("Enter ID to view detail information/b to go back: ", 0 , countID - 1);
		
		System.out.println("============================================================\n");
		
		viewDetailDataSame(element, elementMap.get(readValue));
		
		
	}	
	
	
	/**
	 * View detail data (same).
	 * 
	 * @param element the element
	 * @param node the special element node
	 */
	private void viewDetailDataSame(String element, Node node) {
		
		System.out.println("Detail data of " + node.toString() + ": ");

		StructureClass sElement = diffResolveTool.getStructure().getClasses().get(element);
		
		HashMap<String, ArrayList<Node>> subElementsMapNew = new HashMap<String, ArrayList<Node>>();
		HashMap<String, ArrayList<Node>> subElementsMapOld = new HashMap<String, ArrayList<Node>>();
		
		Iterator<String> subElementsIte = sElement.getProperties().iterator();
    	while (subElementsIte.hasNext()) {
    		String subElementsName = subElementsIte.next();
    		
    		//create query
    		String queryStringSame =	"SELECT ?o " +
    									"WHERE {<" + node.toString() + "> <" + subElementsName + "> ?o }";

    		Query querySame = QueryFactory.create(queryStringSame);
    		
    		//query execution on graph new
			QueryExecution qeSameNew = QueryExecutionFactory.create(querySame, newGraph);
			
			ResultSet resultsSameNew = qeSameNew.execSelect();
			ArrayList<Node> nameNew = new ArrayList<Node>();
			
			//get all sub element attributes
			while(resultsSameNew.hasNext()) {
				QuerySolution qsSameNew = resultsSameNew.next();
					
				// get name						
				try {
					nameNew.add(NodeFactory.createLiteral(qsSameNew.getLiteral("?o").getValue().toString()));
				} catch (ClassCastException e) {
					nameNew.add(NodeFactory.createURI(qsSameNew.getResource("?o").toString()));
				}
				
			}
			subElementsMapNew.put(subElementsName, nameNew);
			
			//query execution on graph new
			QueryExecution qeSameOld = QueryExecutionFactory.create(querySame, oldGraph);
			
			ResultSet resultsSameOld = qeSameOld.execSelect();
			ArrayList<Node> nameOld = new ArrayList<Node>();
			
			//get all sub element attributes
			while(resultsSameOld.hasNext()) {
				QuerySolution qsSameOld = resultsSameOld.next();

				// get name						
				try {
					nameOld.add(NodeFactory.createLiteral(qsSameOld.getLiteral("?o").getValue().toString()));
				} catch (ClassCastException e) {
					nameOld.add(NodeFactory.createURI(qsSameOld.getResource("?o").toString()));
				}

			}
			subElementsMapOld.put(subElementsName, nameOld);

    	}
	    	
       	//generate column width
    	int idWidth = Integer.toString(countDifferencesElement(checkedForDifferencesResult)).length();
		if (idWidth <= 2) {
			idWidth = 2;
		}
		idWidth = 4 + idWidth - 2 + 1;
		int nameWidth = getMaxStringLength(subElementsMapNew.keySet());
		if (nameWidth <= 4) {
			nameWidth = 4;
		}
		nameWidth = idWidth + 6 + nameWidth - 4 + 1;
		int valueNewWidth = getMaxNodeMapLength(subElementsMapNew);
		if (valueNewWidth <= 9) {
			valueNewWidth = 9;
		}
		valueNewWidth = nameWidth + 11 + valueNewWidth - 9 + 1;
		int valueOldWidth = getMaxNodeMapLength(subElementsMapOld);
		if (valueOldWidth <= 9) {
			valueOldWidth = 9;
		}
		valueOldWidth = valueNewWidth + 11 + valueOldWidth - 9 + 1;

			
		//string to print
		String hLine = "";
		String headerLine = "";
		//save id and element
		HashMap<Integer, String> elementMap = new HashMap<Integer, String>();
		
		for (int i=0; i<=valueOldWidth; i++) {
			if ((i == 0) || (i == idWidth) || (i == nameWidth) || (i == valueNewWidth) || (i == valueOldWidth))  {
				hLine = hLine + "+";
				headerLine = headerLine + "|";
			} else {
				hLine = hLine + "-";
				
				if (i == 2) {
					headerLine = headerLine + "ID";
				} else if (i == idWidth + 2) {
					headerLine = headerLine + "name";
				} else if (i == nameWidth + 2) {
					headerLine = headerLine + "new value";
				} else if (i == valueNewWidth + 2) {
					headerLine = headerLine + "old value";
				} else {
					if (headerLine.length() == i) {
						headerLine = headerLine + " ";
					}
				}
			}
		}
		
		System.out.println(hLine);
		System.out.println(headerLine);
		System.out.println(hLine);
			
		String entryLine = "";
		int countID = 0;
		
		Iterator<String> elementIte = subElementsMapNew.keySet().iterator();
		while(elementIte.hasNext()) {
			String nextString = elementIte.next();
			
			//is there a difference? -> add ID
			HashMap<String, ArrayList<Node>> result = diffResolveTool.showDifference(node, NodeFactory.createURI(nextString), newGraph, oldGraph);
			ArrayList<Node> result_new = result.get("new_values");
			ArrayList<Node> result_old = result.get("old_values");
			
			@SuppressWarnings("unchecked")
			ArrayList<Node> differenceList = (ArrayList<Node>) result_new.clone();
			differenceList.removeAll(result_old);
			
			String countIDString = " ";
			
			if (!differenceList.isEmpty()) {
				countIDString = Integer.valueOf(countID).toString();
				elementMap.put(Integer.valueOf(countID), nextString);
				countID++;
			}
			
			Iterator<Node> nodeIteNew = result_new.iterator();
			Iterator<Node> nodeIteOld = result_old.iterator();
			while ((nodeIteNew.hasNext()) || (nodeIteOld.hasNext())) {
				Node nextNodeNew = null;
				String nextNodeNewAsString = " ";
				try {
					nextNodeNew = nodeIteNew.next();
					nextNodeNewAsString = nextNodeNew.toString();
				} catch (NoSuchElementException e) {
					nextNodeNewAsString = " ";
				}
				Node nextNodeOld = null;
				String nextNodeOldAsString = " ";
				try {
					nextNodeOld = nodeIteOld.next();
					nextNodeOldAsString = nextNodeOld.toString();
				} catch (NoSuchElementException e) {
					nextNodeOldAsString = " ";
				}
				
				
				for (int i=0; i<=valueOldWidth; i++) {
					if ((i == 0) || (i == idWidth) || (i == nameWidth) || (i == valueNewWidth) || (i == valueOldWidth))  {
						entryLine = entryLine + "|";
					} else {
						
						if (i == 2) {
							entryLine = entryLine + countIDString;
							countIDString = " ";
						} else if (i == idWidth + 2) {
							entryLine = entryLine + nextString;
							nextString = " ";
						} if (i == nameWidth + 2) {
							entryLine = entryLine + nextNodeNewAsString;
						} else if (i == valueNewWidth + 2) {
							entryLine = entryLine + nextNodeOldAsString;
						} else {
							if (entryLine.length() == i) {
								entryLine = entryLine + " ";
							}
						}
					}
				}
				
				System.out.println(entryLine);
				entryLine = "";
				
			}

		}
		
		System.out.println(hLine);
		System.out.println();
		
		Integer readValue = readDataFromSystemIN("Enter ID to accept or restore data/b to go back: ", 0 , countID - 1);
		
		String selectedElement = elementMap.get(readValue);
		
		System.out.println("You selected ID " + readValue + " - " + selectedElement + ":\n");
		
		String readCommandValue = readCommandDataFromSystemIN("Enter w to accept/d to restore/b to go back: ");
		
		HashMap<String, Model> resultGraphMap = new HashMap<String, Model>();
		
		switch (readCommandValue) {
		case "accept":
			resultGraphMap = diffResolveTool.selectDifference(node, NodeFactory.createURI(selectedElement), newGraph, oldGraph, diffResolveTool.showDifference(node, NodeFactory.createURI(selectedElement), newGraph, oldGraph), true);
			newGraph = resultGraphMap.get("new_model");
			oldGraph = resultGraphMap.get("old_model");		
			break;
		case "restore":
			resultGraphMap = diffResolveTool.selectDifference(node, NodeFactory.createURI(selectedElement), newGraph, oldGraph, diffResolveTool.showDifference(node, NodeFactory.createURI(selectedElement), newGraph, oldGraph), false);
			newGraph = resultGraphMap.get("new_model");
			oldGraph = resultGraphMap.get("old_model");	
			break;
		default:
			break;
		}
				
		System.out.println("============================================================\n");
		
		printListOfAllElements();
    	
	}
	

	/**
	 * Calculates max length of strings in set.
	 * 
	 * @param set the set to check 
	 * @return max length
	 */
	private int getMaxStringLength(Set<String> set) {
		int maxLength = 0;
		Iterator<String> elementIte = set.iterator();
		
		while (elementIte.hasNext()) {
			int length = elementIte.next().length();
			if (length > maxLength) {
				maxLength = length;
			}
		}
		
		return maxLength;
	}
	
	
	/**
	 * Calculates max length of strings in array list of nodes.
	 * 
	 * @param list the list to check 
	 * @return max length
	 */
	private int getMaxNodeStringLength(ArrayList<Node> list) {
		int maxLength = 0;
		Iterator<Node> elementIte = list.iterator();
		
		while (elementIte.hasNext()) {
			int length = elementIte.next().toString().length();
			if (length > maxLength) {
				maxLength = length;
			}
		}
		
		return maxLength;
	}
	
	
	/**
	 * Calculates max length of strings in map of nodes.
	 * 
	 * @param map the map to check 
	 * @return max length
	 */
	private int getMaxNodeMapLength(HashMap<String, ArrayList<Node>> map) {
		int maxLength = 0;
		Iterator<String> elementIte = map.keySet().iterator();
		
		while (elementIte.hasNext()) {
			Iterator<Node> nodeIte = map.get(elementIte.next()).iterator();
			while (nodeIte.hasNext()) {
				int length = nodeIte.next().toString().length();
				if (length > maxLength) {
					maxLength = length;
				}
			}
		}
		
		return maxLength;
	}
	
	
	/**
	 * Count all elements which are different.
	 * 
	 * @param map to check
	 * @return number of different elements
	 */
	private int countDifferencesElement(HashMap<String, Differences> map) {
		int count = 0;
		Iterator<String> elementIte = checkedForDifferencesResult.keySet().iterator();
		
		while(elementIte.hasNext()) {
			String nextElement = elementIte.next();
			if ((!map.get(nextElement).getAdded().isEmpty()) || (!map.get(nextElement).getRemoved().isEmpty())) {
				count ++;
			} else {
				//check sub elements
				Iterator<Node> ite = map.get(nextElement).getDifferences().keySet().iterator();
				while (ite.hasNext()) {
					Node next = ite.next();
					if (!map.get(nextElement).getDifferences().get(next).isEmpty()) {
						count ++;
					}
				}
			}
		}
		
		return count;		
	}

}
