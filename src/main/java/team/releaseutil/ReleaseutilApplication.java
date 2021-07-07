package team.releaseutil;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.List;

@SpringBootApplication
public class ReleaseutilApplication implements CommandLineRunner
	{
	@Value("${release.git.repo.location:/tmp/git-repo}")
	private String gitRepo;
	public static void main(String[] args) {
		SpringApplication.run(ReleaseutilApplication.class, args);
	}

	@Override
	public void run(String... args) throws GitAPIException, IOException
		{
		outputReleaseNotes("", "", ZonedDateTime.now(ZoneOffset.UTC), "");
		}

	public void outputReleaseNotes(String prevReleaseTag, String version, ZonedDateTime deployTime, String rfcRef) throws GitAPIException, IOException
		{
		Git git;
		try {
		System.out.println("Opening git repo " + gitRepo);
		git = Git.open(new File(gitRepo));
		} catch (RepositoryNotFoundException re){
		System.out.println("Unable to find git repo " + gitRepo);
		return;
		}

		List<Ref> tags = git.tagList().call();
		System.out.println("List all tags. Count:  " + tags.size());
		tags.forEach(t -> System.out.println(t.getName()));

		Iterable<RevCommit> logs = git.log().setRevFilter(RevFilter.NO_MERGES).all().call();
		System.out.println("List all logs without merges");
		logs.forEach(r -> {
			ZonedDateTime commitTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(r.getCommitTime()), ZoneOffset.UTC);
			Duration leadTime = Duration.between(commitTime, deployTime);
			System.out.println(
					"id: " + r.getName() +
					", message: " + r.getShortMessage() +
					", time: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(commitTime) +
					", lead time (secs): " + leadTime.getSeconds());
		});

	}
}
