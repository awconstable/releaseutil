package team.releaseutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
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
import java.util.*;

@SpringBootApplication
public class ReleaseutilApplication implements ApplicationRunner
	{
	@Value("${release.git.repo.location:/tmp/git-repo}")
	private String gitRepo;

	public static void main(String[] args) {
		SpringApplication.run(ReleaseutilApplication.class, args);
	}

	public void outputReleaseNotes(String prevReleaseTag, String curReleaseTag, ZonedDateTime deployTime, String rfcRef, String appId, String deployDesc) throws GitAPIException, IOException
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
		System.out.println("List all tags. Count: " + tags.size());

		Optional<Ref> opCurrentTag = tags.stream().filter(t -> t.getName().equals("refs/tags/" + curReleaseTag)).findFirst();
		Optional<Ref> opPrevTag = tags.stream().filter(t -> t.getName().equals("refs/tags/" + prevReleaseTag)).findFirst();
		tags.forEach(t -> System.out.println(t.getName() +  " : " + t.getObjectId().getName()));

		Iterable<RevCommit> logs;
		System.out.println("\n*****   Release Notes   *****");

		if(tags.size() >= 2 && opCurrentTag.isPresent() && opPrevTag.isPresent()) {
			Ref start = opPrevTag.get();
			System.out.println("Start Tag: " + start.getName() + " (" + start.getObjectId().getName() + ")");
			Ref end = opCurrentTag.get();
			System.out.println("End Tag: " + end.getName() + " (" + end.getObjectId().getName() + ")");
			logs = git.log().setRevFilter(RevFilter.NO_MERGES).addRange(start.getObjectId(), end.getObjectId()).call();
		} else if(tags.size() >= 2){
			tags.sort(Comparator.comparingInt(t -> findTagTimeStamp(git, t)));
			System.out.println("No tags specified, using 2 most recent tags");
			Ref start = tags.get(tags.size() - 2);
			System.out.println("Start Tag: " + start.getName() + " (" + start.getObjectId().getName() + ")");
			Ref end = tags.get(tags.size() - 1);
			System.out.println("End Tag: " + end.getName() + " (" + end.getObjectId().getName() + ")");
			logs = git.log().setRevFilter(RevFilter.NO_MERGES).addRange(start.getObjectId(), end.getObjectId()).call();
		} else {
			System.out.println("No tags specified or found, using all commit history");
			logs = git.log().setRevFilter(RevFilter.NO_MERGES).call();
		}

		HashSet<Change> changes = new HashSet<>();
		logs.forEach(r -> {
			ZonedDateTime commitTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(r.getCommitTime()), ZoneOffset.UTC);
			Duration leadTime = Duration.between(commitTime, deployTime);
			changes.add(new Change(r.getName(), Date.from(commitTime.toInstant()), "releaseutil", "commit"));
			System.out.println(
					"commit id: " + r.getName() +
					", message: " + r.getShortMessage() +
					", committed: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(commitTime) +
					", lead time (secs): " + leadTime.getSeconds());
		});
		System.out.println("\n");

		Deployment deployment = new Deployment(curReleaseTag, deployDesc, appId, rfcRef, Date.from(Instant.now()), "releaseutil", changes);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(new File(gitRepo + "/release-notes.json"), deployment);
	}

	private int findTagTimeStamp(Git git, Ref tag)
		{
		Iterable<RevCommit> logs = null;
		try
			{
			logs = git.log().add(tag.getObjectId()).setMaxCount(1).call();
			} catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e)
			{
			e.printStackTrace();
			}
		RevCommit log = null;
		if (logs != null)
			{
			log = logs.iterator().next();
			}
		if(log != null){
	       		return log.getCommitTime();
		   }
	      return 0;
	}

	@Override
	public void run(ApplicationArguments args) throws GitAPIException, IOException
		{
		String prevReleaseTag = "";
		if(args.containsOption("prevReleaseTag")){
			prevReleaseTag = args.getOptionValues("prevReleaseTag").get(0);
		}
		String curReleaseTag;
		if(args.containsOption("curReleaseTag")){
			curReleaseTag = args.getOptionValues("curReleaseTag").get(0);
		} else {
			System.out.println("curReleaseTag argument required");
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
		outputReleaseNotes(prevReleaseTag, curReleaseTag, ZonedDateTime.now(ZoneOffset.UTC), rfcRef, appId, deployDesc);
		}
	}
