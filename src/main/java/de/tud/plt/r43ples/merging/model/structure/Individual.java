package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

/**
 * Table entry of resolution triples table.
 * 
 * @author Markus Graube
 *
 */
public class Individual {
	
	public String resourceUri;
	
	
	public HashMap<Triple, Boolean> triplesBranchA;
	public HashMap<Triple, Boolean> triplesBranchB;
	
}
