package de.tud.plt.r43ples.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;


public class TestRevisionManagment {

	@Before
	public void setUp() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		RevisionManagement.putGraphUnderVersionControl("test1234");
		RevisionManagement.putGraphUnderVersionControl("test_dataset_user");
	}
	
	@After
	public void tearDown() throws HttpException, IOException{
		RevisionManagement.purgeGraph("test1234");
		RevisionManagement.purgeGraph("test_dataset_user");
	}
	
	
	@Test
	public void testR43ples() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber("test1234");
		Assert.assertEquals("0", revNumberMaster);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		
		RevisionManagement.createNewRevision("test1234", "<a> <b> <c>.", "", "test", "test commit message", list, list.get(0));
		
		revNumberMaster = RevisionManagement.getMasterRevisionNumber("test1234");
		Assert.assertEquals("1", revNumberMaster);
		
	}
	
	@Test
	public void testR43ples_user() throws HttpException, IOException {
		ArrayList<String> list = new ArrayList<String>();
		
		StringWriter addSetW = new StringWriter();
		StringWriter deleteSetW = new StringWriter();
		
		list.add("0");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-added-0.nt"), addSetW, "UTF-8");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-removed-0.nt"), deleteSetW, "UTF-8");
		RevisionManagement.createNewRevision("test_dataset_user", addSetW.toString(), deleteSetW.toString(), "test_user", "test commit message 1", list, list.get(0));
				
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber("test_dataset_user");
		Assert.assertEquals("1", revNumberMaster);
		
		
		list.remove("0");
		list.add("1");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-added-1.nt"), addSetW, "UTF-8");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-removed-1.nt"), deleteSetW, "UTF-8");
		RevisionManagement.createNewRevision("test_dataset_user", addSetW.toString(), deleteSetW.toString(), "test_user", "test commit message 2", list, list.get(0));
		
		revNumberMaster = RevisionManagement.getMasterRevisionNumber("test_dataset_user");
		Assert.assertEquals("2", revNumberMaster);
		
		
		list.remove("1");
		list.add("2");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-added-2.nt"), addSetW, "UTF-8");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-removed-2.nt"), deleteSetW, "UTF-8");
		RevisionManagement.createNewRevision("test_dataset_user", addSetW.toString(), deleteSetW.toString(), "test_user", "test commit message 3", list, list.get(0));
		
		revNumberMaster = RevisionManagement.getMasterRevisionNumber("test_dataset_user");
		Assert.assertEquals("3", revNumberMaster);
		
		
		list.remove("2");
		list.add("3");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-added-3.nt"), addSetW, "UTF-8");
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("test-delta-removed-3.nt"), deleteSetW, "UTF-8");
		RevisionManagement.createNewRevision("test_dataset_user", addSetW.toString(), deleteSetW.toString(), "test_user", "test commit message 3", list, list.get(0));
		
		revNumberMaster = RevisionManagement.getMasterRevisionNumber("test_dataset_user");
		Assert.assertEquals("4", revNumberMaster);
	}

}
