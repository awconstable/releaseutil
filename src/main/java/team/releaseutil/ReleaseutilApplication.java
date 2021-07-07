package team.releaseutil;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class ReleaseutilApplication implements CommandLineRunner
	{
	private static final String GIT_REPO = "~/git-repo";
	public static void main(String[] args) {
		SpringApplication.run(ReleaseutilApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, GitAPIException
		{
		Git git;
		try {
			git = Git.open(new File(GIT_REPO));
		} catch (RepositoryNotFoundException re){
			System.out.println("Unable to find git repo " + GIT_REPO);
			return;
		}
		for(RevCommit commit: git.log().all().call()){
		  System.out.println(commit.getName() + " : " + commit.getShortMessage());
		} 
		}
}
