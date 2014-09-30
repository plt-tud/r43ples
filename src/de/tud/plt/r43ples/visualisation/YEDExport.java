package de.tud.plt.r43ples.visualisation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import de.tud.plt.r43ples.revisionTree.NodeSpecification;
import de.tud.plt.r43ples.revisionTree.Tree;

/**
 * Generates a yEd file with all revisions.
 * 
 * @author Stephan Hensel
 *
 */
public class YEDExport {
	
	/* the edge counter (UID) */
	private int edgeCounter = 0;

	/**
	 * The constructor.
	 */
	public YEDExport() {
		this.edgeCounter = 0;
	}
	
	/**
	 * Get file content of yEd file.
	 * 
	 * @param tree the revision tree
	 * @return content of yEd file
	 */
	public String getFileContent(Tree tree, String headRevision) {
		// Define colors
		String yellow = "#FFCC00";
		String red = "#FF0000";
		StringBuilder result = new StringBuilder(
				 		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
						"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">\n" +
						"  <!--Created by yFiles for Java 2.11-->\n" +
						"  <key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>\n" +
						"  <key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>\n" +
						"  <key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>\n" +
						"  <key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>\n" +
						"  <key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>\n" +
						"  <key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>\n" +
						"  <key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>\n" +
						"  <key attr.name=\"Beschreibung\" attr.type=\"string\" for=\"graph\" id=\"d7\"/>\n" +
						"  <key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>\n" +
						"  <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>\n" +
						"  <key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>\n" +
						"  <graph edgedefault=\"directed\" id=\"G\">\n" +
						"    <data key=\"d7\"/>\n");
		// Create nodes and edges
		HashMap<String, NodeSpecification> map = tree.getMap();
		Iterator<String> ite = map.keySet().iterator();
		while (ite.hasNext()) {
			String key = ite.next();
			
			// Create new node
			if (key.equals(headRevision)) {
				result.append(createNode(key, red));
			} else {
				result.append(createNode(key, yellow));
			}
			
			// Create new edges
			Iterator<NodeSpecification> iteEdges = map.get(key).getSuccessors().iterator();
			while (iteEdges.hasNext()) {
				NodeSpecification next = iteEdges.next();
				result.append(createEdge(next.getRevisionNumber(),key));
			}
		}
		
		// If key set is null then there is only the revision 0 (HEAD)
		if ((map.keySet() == null) || (map.keySet().isEmpty())) {
			result.append(createNode("0", red));
		}
		
		result.append(	"  </graph>\n" +
						"  <data key=\"d0\">\n" +
						"    <y:Resources/>\n" +
						"  </data>\n" +
						"</graphml>\n");
		return result.toString();
	}
	
	
	/**
	 * Create new node.
	 * 
	 * @param key the node revision number
	 * @return the node content
	 */
	private String createNode(String key, String color) {
		String resultNode =	"    <node id=\"n" + key + "\">\n" +
							"       <data key=\"d5\"/>\n" +
							"	    <data key=\"d6\">\n" +
							"          <y:ShapeNode>\n" +
							"             <y:Geometry height=\"30.0\" width=\"30.0\" x=\"334.0\" y=\"334.0\"/>\n" +
							"             <y:Fill color=\"" + color + "\" transparent=\"false\"/>\n" +
							"             <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n" +
							"             <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" modelName=\"custom\" textColor=\"#000000\" visible=\"true\" width=\"23.341796875\" x=\"3.3291015625\" y=\"5.6494140625\">" + key + "<y:LabelModel>\n" +
							"                   <y:SmartNodeLabelModel distance=\"4.0\"/>\n" +
							"                </y:LabelModel>\n" +
							"                <y:ModelParameter>\n" +
							"                   <y:SmartNodeLabelModelParameter labelRatioX=\"0.0\" labelRatioY=\"0.0\" nodeRatioX=\"0.0\" nodeRatioY=\"0.0\" offsetX=\"0.0\" offsetY=\"0.0\" upX=\"0.0\" upY=\"-1.0\"/>\n" +
							"                </y:ModelParameter>\n" +
							"             </y:NodeLabel>\n" +
							"             <y:Shape type=\"ellipse\"/>\n" +
							"          </y:ShapeNode>\n" +
							"       </data>\n" +
							"    </node>\n";
		return resultNode;
	}
	
	
	/**
	 * Create new edge.
	 * 
	 * @param string the source node revision number
	 * @param key the target node revision number
	 * @return the edge content
	 */
	private String createEdge(String string, String key) {
		String resultEdge =	"	<edge id=\"e" + edgeCounter + "\" source=\"n" + string + "\" target=\"n" + key + "\">\n" +
							"      <data key=\"d8\"/>\n" +
							"      <data key=\"d9\"/>\n" +
							"      <data key=\"d10\">\n" +
							"        <y:PolyLineEdge>\n" +
							"          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n" +
							"          <y:LineStyle color=\"#000000\" type=\"dashed\" width=\"1.0\"/>\n" +
							"          <y:Arrows source=\"none\" target=\"plain\"/>\n" +
							"          <y:BendStyle smoothed=\"false\"/>\n" +
							"        </y:PolyLineEdge>\n" +
							"      </data>\n" +
							"    </edge>\n";
		
		edgeCounter++;
		
		return resultEdge;
	}
	
	
	/**
	 * Write a string to file.
	 * 
	 * @param filePath the file path
	 * @param data the data to write
	 * @throws IOException
	 */
	public void writeStringToFile(String filePath, String data) throws IOException {
		FileUtils.writeStringToFile(new File(filePath), data);
	}
	
	
	/**
	 * Write yEd data to file.
	 * 
	 * @param tree the revision tree
	 * @param filePath the file path
	 * @throws IOException 
	 */
	public void writeYEDDataToFile(Tree tree, String headRevision, String filePath) throws IOException {
		writeStringToFile(filePath, getFileContent(tree, headRevision));
	}

	
}