package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpException;

public class SampleDataSet {
	
	public static void createSampleDataset1(String graph) throws HttpException, IOException{
		RevisionManagement.purgeGraph(graph);
		RevisionManagement.putGraphUnderVersionControl(graph);

		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-1.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-1.nt"), "test_user",
				"test commit message 1", list);
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-2.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-2.nt"), "test_user",
				"test commit message 2", list);
		list.remove("1");
		list.add("2");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-3.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-3.nt"), "test_user",
				"test commit message 3", list);
		list.remove("2");
		list.add("3");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-4.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-4.nt"), "test_user",
				"test commit message 4", list);
	}
	
	
	public static void createSampleDataset2(String graph) throws HttpException, IOException{
		RevisionManagement.purgeGraph(graph);
		RevisionManagement.putGraphUnderVersionControl(graph);

		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-delta-added-1.nt"),
				ResourceManagement.getContentFromResource("samples/test2-delta-removed-1.nt"), "test_user",
				"test commit message 1", list);
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-delta-added-2.nt"),
				ResourceManagement.getContentFromResource("samples/test2-delta-removed-2.nt"), "test_user",
				"test commit message 2", list);
	}

}
