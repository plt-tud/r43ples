/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.junit.*;

import static org.junit.Assert.*;

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
        String query = "SELECT * WHERE { GRAPH <http://test.com/r43ples-dataset-1> REVISION \"master\" { ?s ?p ?o}}";
        String result = hi.getResponseHeaderFromQuery(query);
        Assert.assertEquals("@prefix rmo:   <http://eatld.et.tu-dresden.de/rmo#> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-revision-5>\n" +
                "        rmo:revisionNumber  \"5\" .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-master>\n" +
                "        a               rmo:Branch , rmo:Master ;\n" +
                "        rdfs:label      \"master\" ;\n" +
                "        rmo:references  <http://test.com/r43ples-dataset-1-revision-5> .\n", result);

        String query2 = "SELECT * WHERE { " +
                "GRAPH <http://test.com/r43ples-dataset-1> REVISION \"1\" { ?s ?p ?o}" +
                "GRAPH <http://test.com/r43ples-dataset-2> REVISION \"2\" { ?s ?p ?o}" +
                "}";
        String result2 = hi.getResponseHeaderFromQuery(query2);
        Assert.assertEquals("@prefix rmo:   <http://eatld.et.tu-dresden.de/rmo#> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-2-master>\n" +
                "        a               rmo:Branch , rmo:Master ;\n" +
                "        rdfs:label      \"master\" ;\n" +
                "        rmo:references  <http://test.com/r43ples-dataset-2-revision-3> .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-revision-5>\n" +
                "        rmo:revisionNumber  \"5\" .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-master>\n" +
                "        a               rmo:Branch , rmo:Master ;\n" +
                "        rdfs:label      \"master\" ;\n" +
                "        rmo:references  <http://test.com/r43ples-dataset-1-revision-5> .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-2-revision-3>\n" +
                "        rmo:revisionNumber  \"3\" .\n", result2);
    }


    @Test
    public void checkUpToDate() throws InternalErrorException {

        String updateQuery = "UPDATE GRAPH <http://test.com/r43ples-dataset-1> REVISION \"master\"";

        hi.checkUpToDate("@prefix rmo:   <http://eatld.et.tu-dresden.de/rmo#> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-revision-5>\n" +
                "        rmo:revisionNumber  \"5\" .\n" +
                "\n" +
                "<http://test.com/r43ples-dataset-1-master>\n" +
                "        a               rmo:Branch , rmo:Master ;\n" +
                "        rdfs:label      \"master\" ;\n" +
                "        rmo:references  <http://test.com/r43ples-dataset-1-revision-5> .\n",
                updateQuery);

        hi.checkUpToDate("" +
                        "<http://test.com/r43ples-dataset-1-master>\n" +
                        "        a               <http://eatld.et.tu-dresden.de/rmo#Branch> , <http://eatld.et.tu-dresden.de/rmo#Master> ;\n" +
                        "        <http://www.w3.org/2000/01/rdf-schema#label>      \"master\" ;\n" +
                        "        <http://eatld.et.tu-dresden.de/rmo#references>  <http://test.com/r43ples-dataset-1-revision-5> .\n" +
                        "<http://test.com/r43ples-dataset-1-revision-5>\n" +
                        "        <http://eatld.et.tu-dresden.de/rmo#revisionNumber>  \"5\" .\n",
                updateQuery);


        try {
            hi.checkUpToDate("@prefix rmo:   <http://eatld.et.tu-dresden.de/rmo#> .\n" +
                            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                            "\n" +
                            "<http://test.com/r43ples-dataset-1-revision-5>\n" +
                            "        rmo:revisionNumber  \"4\" .\n" +
                            "\n" +
                            "<http://test.com/r43ples-dataset-1-master>\n" +
                            "        a               rmo:Branch , rmo:Master ;\n" +
                            "        rdfs:label      \"master\" ;\n" +
                            "        rmo:references  <http://test.com/r43ples-dataset-1-revision-5> .\n",
                    updateQuery);
            Assert.fail("Should throw exception since revision information is out of date");
        } catch (OutdatedException e) {

        }




    }

}