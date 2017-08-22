package de.tud.plt.r43ples.management;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class GitRepositoryState {

	private static GitRepositoryState gitRepositoryState = null;

	public String branch; // =${git.branch}
	public String describe; // =${git.commit.id.describe}
	public String describeShort; // =${git.commit.id.describe-short}
	public String commitId; // =${git.commit.id}
	public String commitIdAbbrev; // =${git.commit.id.abbrev}
	public String commitTime; // =${git.commit.time}

	private GitRepositoryState(PropertiesConfiguration properties) {
		this.branch = properties.getString("git.branch");
		this.describe = properties.getString("git.commit.id.describe");
		this.describeShort = properties.getString("git.commit.id.describe-short");
		this.commitId = properties.getString("git.commit.id");
		this.commitIdAbbrev = properties.getString("git.commit.id.abbrev");
		this.commitTime = properties.getString("git.commit.time");
	}

	public static GitRepositoryState getGitRepositoryState() {

		if (gitRepositoryState == null) {
			PropertiesConfiguration config;
			try {
				config = new PropertiesConfiguration("git.properties");
				gitRepositoryState = new GitRepositoryState(config);
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}

		}
		return gitRepositoryState;
	}
}