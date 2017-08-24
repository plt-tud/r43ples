/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HeaderInformationTest extends R43plesTest {
    private static DataSetGenerationResult ds1;
    private static HeaderInformation hi;

    @BeforeClass
    public static void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
        SampleDataSet.createSampleDataset1();
        SampleDataSet.createSampleDataset2();
        hi = new HeaderInformation();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TripleStoreInterfaceSingleton.close();
    }

    @Test
    public void getResponseHeaderFromQuery() throws Exception {
        String query = ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/query1.rq");
        String result = hi.getResponseHeaderFromQuery(query);
        String expected = ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/revisiongraph_dataset1.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        String query2 = ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/query2.rq");
        String result2 = hi.getResponseHeaderFromQuery(query2);
        String expected2 = ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/revisiongraph_dataset12.ttl");
        Assert.assertTrue(this.check_isomorphism(result2, expected2));
    }


    @Test
    public void checkUpToDate() throws InternalErrorException {

        String updateQuery = ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/update_query.rq");

        hi.checkUpToDate(ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/revisiongraph_dataset1.ttl"),
                updateQuery);

        hi.checkUpToDate(ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/revisiongraph_dataset1_alternative.ttl"),
                updateQuery);

        try {
            hi.checkUpToDate(ResourceManagement.getContentFromResource("draftobjects/HeaderInformation/revisiongraph_dataset1_outdated.ttl"),
                    updateQuery);
            Assert.fail("Should throw exception since revision information is out of date");
        } catch (OutdatedException e) {

        }

    }

}