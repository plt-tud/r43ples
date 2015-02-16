package de.tud.plt.r43ples.examples;


import com.beust.jcommander.Parameter;

public class JCommanderImpl {
  
  @Parameter(names = {"-g", "--graph"}, required=true, description = "uri of graph")
  public String graph;

  @Parameter(names = {"-c", "--create"}, description = "create graph")
  public boolean create;
  
  @Parameter(names = {"-a", "--add-set"}, description = "add set file")
  public String add_set;

  @Parameter(names = {"-d", "--delete-set"}, description = "delete set file")
  public String delete_set;
  
  @Parameter(names = {"-h", "--help"}, description = "shows help", help = true)
  public boolean help;


}