package de.tud.plt.r43ples.optimization;

import org.apache.log4j.Logger;

/**
 * The path calculation singleton.
 *
 * @author Stephan Hensel
 */
public class PathCalculationSingleton {

    /** The logger */
    private static Logger logger = Logger.getLogger(PathCalculationSingleton.class);

    /** The path calculation object. **/
    private static PathCalculationInterface pathCalculation;


    /**
     * The constructor.
     */
    private PathCalculationSingleton() {

    }

    /**
     * Get the instance of the currently chosen path calculation.
     *
     * @return the instance of the path calculation implementation
     */
    public static PathCalculationInterface getInstance() {
        if (pathCalculation!=null)
            return pathCalculation;
        else {
            // TODO add configuration possibilities
            logger.info("Simple path calculations will be used.");
            pathCalculation = new SimplePathCalculation();
            return pathCalculation;
        }
    }

}
