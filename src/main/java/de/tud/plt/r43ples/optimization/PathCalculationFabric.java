package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import org.apache.log4j.Logger;

/**
 * The path calculation singleton.
 *
 * @author Stephan Hensel
 */
public class PathCalculationFabric {

    /** The logger */
    private static Logger logger = Logger.getLogger(PathCalculationFabric.class);


    /**
     * The constructor.
     */
    private PathCalculationFabric() {

    }

    /**
     * Get the instance of the currently chosen path calculation.
     *
     * @return the instance of the path calculation implementation
     */
    public static PathCalculationInterface getInstance(RevisionGraph revisionGraph) {
        logger.info("Simple path calculations will be used.");
        return new SimplePathCalculation(revisionGraph);
    }

}
