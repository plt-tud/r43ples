package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.management.R43plesRequest;

/**
 * Collection of information for creating a new merge commit.
 *
 * @author Stephan Hensel
 */
public class MergeCommitDraft extends CommitDraft {


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     */
    public MergeCommitDraft(R43plesRequest request) throws OutdatedException {
        super(request);
    }

    // TODO

}