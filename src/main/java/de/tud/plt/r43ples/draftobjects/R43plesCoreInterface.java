package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.ThreeWayMergeCommit;
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
     * Create a new initial commit.
     *
     * @param request the request received by R43ples
     * @return the created initial commit
     * @throws InternalErrorException
     */
    InitialCommit createInitialCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new initial commit..
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param derivedFromIdentifier the revision identifier of the revision or the reference identifier from which the new revision should be derive from
     * @return the created update commit
     * @throws InternalErrorException
     */
    InitialCommit createInitialCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException;


    /**
     * Create a new update commit.
     *
     * @param request the request received by R43ples
     * @return the list of created update commits
     * @throws InternalErrorException
     */
    ArrayList<UpdateCommit> createUpdateCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new update commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param derivedFromIdentifier the revision identifier of the revision or the reference identifier from which the new revision should be derive from
     * @return the created update commit
     * @throws InternalErrorException
     */
    UpdateCommit createUpdateCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException;


    /**
     * Create a new three way merge commit.
     *
     * @param request the request received by R43ples
     * @return the created three way merge commit
     * @throws InternalErrorException
     */
    ThreeWayMergeCommit createThreeWayMergeCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new three way merge commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param derivedFromIdentifierSource the source revision identifier of the revision or the reference identifier from which the new revision should be derive from
     * @param derivedFromIdentifierTarget the target revision identifier of the revision or the reference identifier from which the new revision should be derive from, the corresponding branch will be used to store the new revision
     * @return the created three way merge commit
     * @throws InternalErrorException
     */
    ThreeWayMergeCommit createThreeWayMergeCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifierSource, String derivedFromIdentifierTarget) throws InternalErrorException;

}