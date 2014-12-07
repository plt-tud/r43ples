package de.tud.plt.r43ples.management;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class GitRepositoryState {
	
	private static GitRepositoryState gitRepositoryState = null;

	 String branch;                  // =${git.branch}
	 String tags;                    // =${git.tags} // comma separated tag names
	 String describe;                // =${git.commit.id.describe}
	 String describeShort;           // =${git.commit.id.describe-short}
	 String commitId;                // =${git.commit.id}
	 String commitIdAbbrev;          // =${git.commit.id.abbrev}
	 String buildUserName;           // =${git.build.user.name}
	 String buildUserEmail;          // =${git.build.user.email}
	 String buildTime;               // =${git.build.time}
	 String commitUserName;          // =${git.commit.user.name}
	 String commitUserEmail;         // =${git.commit.user.email}
	 String commitMessageFull;       // =${git.commit.message.full}
	 String commitMessageShort;      // =${git.commit.message.short}
	 String commitTime;              // =${git.commit.time}

	public GitRepositoryState(PropertiesConfiguration properties)
	{
	   this.branch = properties.getString("git.branch");
	   this.tags = properties.getString("git.tags");
	   this.describe = properties.getString("git.commit.id.describe");
	   this.describeShort = properties.getString("git.commit.id.describe-short");
	   this.commitId = properties.getString("git.commit.id");
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
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	      gitRepositoryState = new GitRepositoryState(config);
	   }
	   return gitRepositoryState;
	}
}