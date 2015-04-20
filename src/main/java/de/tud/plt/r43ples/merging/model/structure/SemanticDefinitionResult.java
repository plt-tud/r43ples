package de.tud.plt.r43ples.merging.model.structure;

import java.util.ArrayList;

/**
 * Stores the semantic definition result.
 * 
 * @author Stephan Hensel
 *
 */
public class SemanticDefinitionResult {

	/** The semantic description. **/
	private String semanticDescription;
	/** The semantic resolution options. **/
	private ArrayList<String> semanticResolutionOptions;
	/** The selected semantic resolution option. **/
	private int selectedSemanticResolutionOption;
	
	
	/**
	 * The constructor.
	 * 
	 * @param semanticDescription the semantic description
	 * @param semanticResolutionOptions the semantic resolution options
	 * @param selectedSemanticResolutionOption the selected semantic resolution option
	 */
	public SemanticDefinitionResult(String semanticDescription, ArrayList<String> semanticResolutionOptions, int selectedSemanticResolutionOption) {
		this.semanticDescription = semanticDescription;
		this.semanticResolutionOptions = semanticResolutionOptions;
		this.selectedSemanticResolutionOption = selectedSemanticResolutionOption;
	}


	/**
	 * Get the semantic description.
	 * 
	 * @return the semantic description
	 */
	public String getSemanticDescription() {
		return semanticDescription;
	}


	/**
	 * Set the semantic description.
	 * 
	 * @param semanticDescription the semantic description to set
	 */
	public void setSemanticDescription(String semanticDescription) {
		this.semanticDescription = semanticDescription;
	}


	/**
	 * Get the semantic resolution options.
	 * 
	 * @return the semantic resolution options
	 */
	public ArrayList<String> getSemanticResolutionOptions() {
		return semanticResolutionOptions;
	}


	/**
	 * Set the semantic resolution options.
	 * 
	 * @param semanticResolutionOptions the semantic resolution options to set
	 */
	public void setSemanticResolutionOptions(
			ArrayList<String> semanticResolutionOptions) {
		this.semanticResolutionOptions = semanticResolutionOptions;
	}


	/**
	 * Get the selected semantic resolution option.
	 * 
	 * @return the selected semantic resolution option
	 */
	public int getSelectedSemanticResolutionOption() {
		return selectedSemanticResolutionOption;
	}


	/**
	 * Set the selected semantic resolution option.
	 * 
	 * @param selectedSemanticResolutionOption the selected semantic resolution option to set
	 */
	public void setSelectedSemanticResolutionOption(int selectedSemanticResolutionOption) {
		this.selectedSemanticResolutionOption = selectedSemanticResolutionOption;
	}
	
}
