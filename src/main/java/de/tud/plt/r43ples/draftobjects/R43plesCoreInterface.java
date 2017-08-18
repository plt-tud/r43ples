package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.UpdateCommit;
import de.tud.plt.r43ples.management.R43plesRequest;

import java.util.ArrayList;

/**
 * This interface provides methods to access the core functions of R43ples.
 *
 * @author Stephan Hensel
 *
 */
public interface R43plesCoreInterface {

    /**
     * Create a new update Commit.
     *
     * @param request the request received by R43ples
     * @return the list of created update commits
     * @throws InternalErrorException
     */
    ArrayList<UpdateCommit> createUpdateCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new update Commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param derivedFromIdentifier the revision identifier of the revision or the reference identifier from which the new revision should be derive from
     * @return the list of created update commits
     * @throws InternalErrorException
     */
    ArrayList<UpdateCommit> createUpdateCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException;

}