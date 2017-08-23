package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.management.R43plesRequest;

/**
 * Collection of information for creating a new fast forward merge commit.
 *
 * @author Stephan Hensel
 */
public class FastForwardMergeCommitDraft extends MergeCommitDraft {


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public FastForwardMergeCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
    }

    // TODO

}