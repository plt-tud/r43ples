package de.tud.plt.r43ples.test;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.auth.AuthenticationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;


public class TestRevisionManagment {

	@Before
	public void setUp() throws AuthenticationException, IOException, ConfigurationException{
		Config.readConfig("Service.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		RevisionManagement.putGraphUnderVersionControl("test1234");
	}
	
	@After
	public void tearDown() throws AuthenticationException, IOException{
		RevisionManagement.purgeGraph("test1234");
	}
	
	
	@Test
	public void testR43ples() throws AuthenticationException, IOException {
		RevisionManagement.putGraphUnderVersionControl("test1234");
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber("test1234");
		Assert.assertEquals("0", revNumberMaster);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		
		RevisionManagement.createNewRevision("test1234", "<a> <b> <c>.", "", "test", "1", "test commit message", list);
		
		revNumberMaster = RevisionManagement.getMasterRevisionNumber("test1234");
		Assert.assertEquals("1", revNumberMaster);
		
	}

}
