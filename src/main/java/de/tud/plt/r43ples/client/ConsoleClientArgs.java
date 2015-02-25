package de.tud.plt.r43ples.client;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class ConsoleClientArgs {
  
  @Parameter(names = {"-g", "--graph"}, required=true, description = "uri of graph")
  public String graph;

  @Parameter(names = {"--create"}, description = "create graph")
  public boolean create;
  
  @Parameter(names = {"-a", "--add-set"}, description = "add set file")
  public String add_set;

  @Parameter(names = {"-d", "--delete-set"}, description = "delete set file")
  public String delete_set;
  
  
  @ParametersDelegate
  public R43plesArgs r43ples = new R43plesArgs();

}