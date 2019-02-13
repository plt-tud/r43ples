/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

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

import static org.hamcrest.core.StringContains.containsString;

public class HeaderInformationTest extends R43plesTest {
    private static DataSetGenerationResult ds1;
    private static DataSetGenerationResult ds2;
    private static HeaderInformation hi;

    @BeforeClass
    public static void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
        ds1 = SampleDataSet.createSampleDataset1();
        ds2 = SampleDataSet.createSampleDataset2();
        hi = new HeaderInformation();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TripleStoreInterfaceSingleton.close();
    }

    @Test
    public void getResponseHeaderFromQuery() throws Exception {
        String query = ResourceManagement.getContentFromResource("core/HeaderInformation/query1.rq");
        String result = hi.getResponseHeaderFromQuery(query);
        String expected = ResourceManagement.getContentFromResource("core/HeaderInformation/revisiongraph_dataset1.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        String query2 = ResourceManagement.getContentFromResource("core/HeaderInformation/query2.rq");
        String result2 = hi.getResponseHeaderFromQuery(query2);
        String expected2 = ResourceManagement.getContentFromResource("core/HeaderInformation/revisiongraph_dataset12.ttl");
        Assert.assertTrue(this.check_isomorphism(result2, expected2));
    }


    @Test
    public void checkUpToDate() throws InternalErrorException {

        String updateQuery = ResourceManagement.getContentFromResource("core/HeaderInformation/update_query.rq");

        hi.checkUpToDate(ResourceManagement.getContentFromResource("core/HeaderInformation/revisiongraph_dataset1.ttl"),
                updateQuery);

        hi.checkUpToDate(ResourceManagement.getContentFromResource("core/HeaderInformation/revisiongraph_dataset1_alternative.ttl"),
                updateQuery);

        try {
            hi.checkUpToDate(ResourceManagement.getContentFromResource("core/HeaderInformation/revisiongraph_dataset1_outdated.ttl"),
                    updateQuery);
            Assert.fail("Should throw exception since revision information is out of date");
        } catch (OutdatedException e) {

        }

    }

    @Test
    public void testResponseHeader() {
        String sparql = "SELECT *"
                + "FROM <" + ds1.graphName + ">"
                + "WHERE { ?s ?p ?o}";

        String result = new HeaderInformation().getResponseHeaderFromQuery(sparql);
        Assert.assertThat(result, containsString("Master"));
    }

    @Test
    public void testResponseHeader2() {
        String sparql = "SELECT *"
                + "FROM <" + ds1.graphName + ">"
                + "FROM <" + ds2.graphName + ">"
                + "WHERE { ?s ?p ?o}";

        String result = new HeaderInformation().getResponseHeaderFromQuery(sparql);
        Assert.assertThat(result, containsString("Master"));
    }

}