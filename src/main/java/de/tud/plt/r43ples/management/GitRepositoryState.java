package de.tud.plt.r43ples.management;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class GitRepositoryState {
	
	private static GitRepositoryState gitRepositoryState = null;

	public String branch;                  // =${git.branch}
	public String tags;                    // =${git.tags} // comma separated tag names
	public String describe;                // =${git.commit.id.describe}
	public String describeShort;           // =${git.commit.id.describe-short}
	public String commitId;                // =${git.commit.id}
	public String commitIdAbbrev;          // =${git.commit.id.abbrev}
	public String buildUserName;           // =${git.build.user.name}
	public String buildUserEmail;          // =${git.build.user.email}
	public String buildTime;               // =${git.build.time}
	public String commitUserName;          // =${git.commit.user.name}
	public String commitUserEmail;         // =${git.commit.user.email}
	public String commitMessageFull;       // =${git.commit.message.full}
	public String commitMessageShort;      // =${git.commit.message.short}
	public String commitTime;              // =${git.commit.time}

	private GitRepositoryState()
	{
	}

	
	private GitRepositoryState(PropertiesConfiguration properties)
	{
	   this.branch = properties.getString("git.branch");
	   this.tags = properties.getString("git.tags");
	   this.describe = properties.getString("git.commit.id.describe");
	   this.describeShort = properties.getString("git.commit.id.describe-short");
	   this.commitId = properties.getString("git.commit.id");
	   this.commitIdAbbrev = properties.getString("git.commit.id.abbrev");
	   this.buildUserName = properties.getString("git.build.user.name");
	   this.buildUserEmail = properties.getString("git.build.user.email");
	   this.buildTime = properties.getString("git.build.time");
	   this.commitUserName = properties.getString("git.commit.user.name");
	   this.commitUserEmail = properties.getString("git.commit.user.email");
	   this.commitMessageShort = properties.getString("git.commit.message.short");
	   this.commitMessageFull = properties.getString("git.commit.message.full");
	   this.commitTime = properties.getString("git.commit.time");
	}

	
	public static GitRepositoryState getGitRepositoryState()
	{

	if (gitRepositoryState == null)
	   {
		 PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration("git.properties");
			gitRepositoryState = new GitRepositoryState(config);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			gitRepositoryState = new GitRepositoryState();
		}
	      
	   }
	   return gitRepositoryState;
	}
}