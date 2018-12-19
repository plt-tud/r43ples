package de.tud.plt.r43ples.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The R43ples core singleton.
 *
 * @author Stephan Hensel
 */
public class R43plesCoreSingleton {

    /** The logger */
    private static Logger logger = LogManager.getLogger(R43plesCoreSingleton.class);

    /** The R43ples core object. **/
    private static R43plesCoreInterface r43plesCore;


    /**
     * The constructor.
     */
    private R43plesCoreSingleton() {

    }

    /**
     * Get the instance of R43ples core.
     *
     * @return the instance of R43ples core
     */
    public static R43plesCoreInterface getInstance() {
        if (r43plesCore!=null)
            return r43plesCore;
        else {
            r43plesCore = new R43plesCore();
            return r43plesCore;
        }
    }

}
