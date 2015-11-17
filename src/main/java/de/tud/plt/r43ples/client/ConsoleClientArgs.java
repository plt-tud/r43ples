package de.tud.plt.r43ples.client;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.tud.plt.r43ples.management.RevisionManagement;

public class ConsoleClientArgs {
  
  @Parameter(names = {"-g", "--graph"}, required=true, description = "uri of graph")
  public String graph;

  @Parameter(names = {"-n", "--new"}, description = "Creates new graph after deleting the graph when already existing")
  public boolean create;
  
  @Parameter(names = {"-a", "--add-set"}, description = "add set file")
  public String add_set;

  @Parameter(names = {"-d", "--delete-set"}, description = "delete set file")
  public String delete_set;
  
  @Parameter(names = {"-u", "--user"}, description = "user name")
  public String user = "console client";
  
  @Parameter(names = {"-t", "--timestamp"}, description = "time stamp (default: current time stamp)")
  public String time_stamp = RevisionManagement.getDateString();
  
  @Parameter(names = {"-m", "--message"}, description = "commit message")
  public String message = "console client commit";
  
  @Parameter(names = {"-b", "--branch"}, description = "branch which should contain new revision")
  public String branch = "master";
  
  @ParametersDelegate
  public R43plesArgs r43ples = new R43plesArgs();

}