package team.releaseutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import team.releaseutil.model.Change;
import team.releaseutil.model.Deployment;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
public class ReleaseutilApplication implements ApplicationRunner
	{
	@Value("${release.git.repo.location:/tmp/git-repo}")
	private String gitRepo;

	public static void main(String[] args) {
		SpringApplication.run(ReleaseutilApplication.class, args);
	}

	public void outputReleaseNotes(String prevReleaseTag, String version, ZonedDateTime deployTime, String rfcRef, String appId, String deployDesc) throws GitAPIException, IOException
		{
		Git git;
		try {
			System.out.println("Opening git repo " + gitRepo+ "\n");
			git = Git.open(new File(gitRepo));
		} catch (RepositoryNotFoundException re){
			System.out.println("Unable to find git repo " + gitRepo+ "\n");
			return;
		}

		List<Ref> tags = git.tagList().call();
		System.out.println("List all tags. Count:  " + tags.size() + "\n");
		tags.forEach(t -> System.out.println(t.getName()));

		Iterable<RevCommit> logs = git.log().setRevFilter(RevFilter.NO_MERGES).all().call();
		System.out.println("Release Notes\n");
		HashSet<Change> changes = new HashSet<>();
		logs.forEach(r -> {
			ZonedDateTime commitTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(r.getCommitTime()), ZoneOffset.UTC);
			Duration leadTime = Duration.between(commitTime, deployTime);
			changes.add(new Change(r.getName(), Date.from(commitTime.toInstant()), "releaseutil", "commit"));
			System.out.println(
					"commit id: " + r.getName() +
					", short message: " + r.getShortMessage() +
					", commit time: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(commitTime) +
					", lead time (secs): " + leadTime.getSeconds());
		});
		System.out.println("\n");

		Deployment deployment = new Deployment(version, deployDesc, appId, rfcRef, Date.from(Instant.now()), "releaseutil", changes);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(new File(gitRepo + "/release-notes.json"), deployment);
	}

	@Override
	public void run(ApplicationArguments args) throws GitAPIException, IOException
		{
		String prevReleaseTag = "";
		if(args.containsOption("prevReleaseTag")){
			prevReleaseTag = args.getOptionValues("prevReleaseTag").get(0);
		}
		String version;
		if(args.containsOption("version")){
			version = args.getOptionValues("version").get(0);
		} else {
			System.out.println("version argument required");
			return;
		}
		String rfcRef;
		if(args.containsOption("rfcRef")){
			rfcRef = args.getOptionValues("rfcRef").get(0);
		} else {
			System.out.println("rfcRef argument required");
			return;
		}
		String appId;
		if(args.containsOption("appId")){
			appId = args.getOptionValues("appId").get(0);
		} else {
			System.out.println("appId argument required");
			return;
		}
		String deployDesc;
		if(args.containsOption("deployDesc")){
			deployDesc = args.getOptionValues("deployDesc").get(0);
		} else {
			System.out.println("deployDesc argument required");
			return;
		}
		outputReleaseNotes(prevReleaseTag, version, ZonedDateTime.now(ZoneOffset.UTC), rfcRef, appId, deployDesc);
		}
	}
