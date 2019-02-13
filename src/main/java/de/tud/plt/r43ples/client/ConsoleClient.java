package de.tud.plt.r43ples.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import de.tud.plt.r43ples.core.R43plesCoreInterface;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.UpdateCommit;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;


public class ConsoleClient {

    private static Logger logger = LogManager.getLogger(ConsoleClient.class);

    public static void main(String[] args) throws ConfigurationException, IOException, InternalErrorException {


        // command-line-parser: JCommander-1.29
        ConsoleClientArgs args_client = new ConsoleClientArgs();
        JCommander jc = new JCommander(args_client);

        // wrong input => display usage
        try {
            jc.setProgramName("de.tud.plt.r43ples.client.ConsoleClient");
            jc.parse(args);
        } catch (ParameterException e) {
            jc.usage();
            System.err.println(e.toString());
            return;
        }

        if (args_client.r43ples.help) {
            jc.usage();
            return;
        }

        // TODO: add file import option for generating new version (only new version instead of add and delete sets)

        logger.info("config: " + args_client.r43ples.config);
        logger.info("graph: " + args_client.graph);
        logger.info("create: " + args_client.create);
        logger.info("add set file: " + args_client.add_set);
        logger.info("delete set file: " + args_client.delete_set);
        logger.info("user: " + args_client.user);
        logger.info("commit message: " + args_client.message);
        logger.info("branch: " + args_client.branch);


        Config.readConfig(args_client.r43ples.config);
        R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

        if (args_client.create) {
            RevisionGraph graph = new RevisionGraph(args_client.graph);
            graph.purgeRevisionInformation();
            TripleStoreInterfaceSingleton.get().executeCreateGraph(args_client.graph);

            r43plesCore.createInitialCommit(args_client.graph, null, null, "Console client", "Create graph");

            logger.info("Graph created: " + args_client.graph);
        } else {
            String addSet = readFile(args_client.add_set);
            String deleteSet = readFile(args_client.delete_set);

            UpdateCommit commit1 = r43plesCore.createUpdateCommit(args_client.graph, addSet, deleteSet, args_client.user, args_client.message, args_client.branch);
            String result = commit1.getGeneratedRevision().getRevisionIdentifier();

            logger.info("New Revision: " + result);
        }
    }

    /**
     * @param filename
     * @return
     * @throws IOException
     */
    private static String readFile(String filename) throws IOException {
        if (filename != null) {
            File inputFile = new File(filename);

            if (!inputFile.canRead() || !inputFile.isFile()) {
                System.err
                        .println("Wrong input file: There was no file found at "
                                + filename + "!");
                System.exit(0);
            }
            return FileUtils.readFileToString(inputFile);
        } else
            return "";
    }

}
