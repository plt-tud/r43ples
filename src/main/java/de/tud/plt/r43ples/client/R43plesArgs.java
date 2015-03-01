package de.tud.plt.r43ples.client;


import com.beust.jcommander.Parameter;

public class R43plesArgs {
  
  @Parameter(names = {"-c", "--config"}, description = "path to config file")
  public String config = "r43ples.conf";
  
  @Parameter(names = {"-h", "--help"}, description = "shows help", help = true)
  public boolean help;


}