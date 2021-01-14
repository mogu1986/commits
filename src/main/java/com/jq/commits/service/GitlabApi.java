package com.jq.commits.service;

import com.jq.commits.entity.Commit;
import com.jq.commits.entity.Project;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GitlabApi {


    public static List<Commit> list(Date start, Date end, Project project) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("http://gitlab.top.mw", "N99pyaZem6sqbjvUPHSi");
        List<org.gitlab4j.api.models.Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), project.getBranch(), start, end);
        return null;
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2021-01-01 00:00:00");
        Date end = sdf.parse("2021-01-13 23:59:59");

        GitLabApi gitLabApi = new GitLabApi("http://gitlab.top.mw", "N99pyaZem6sqbjvUPHSi");
        List<org.gitlab4j.api.models.Commit> commits = gitLabApi.getCommitsApi().getCommits(273,"master", start, end);
        System.out.println(commits.size());
    }

}
