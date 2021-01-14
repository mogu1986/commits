package com.jq.commits.service;

import com.jq.commits.entity.Commit;
import com.jq.commits.entity.Project;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import java.util.Date;
import java.util.List;

public class GitlabApi {


    public static List<Commit> list(Date start, Date end, Project project) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("http://gitlab.top.mw", "N99pyaZem6sqbjvUPHSi");
        List<org.gitlab4j.api.models.Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), project.getBranch(), start, end);
        return null;
    }

}
