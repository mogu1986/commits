package com.jq.commits.service;

import com.jq.commits.entity.Commit;
import com.jq.commits.entity.Project;
import org.assertj.core.util.Lists;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CommitService {

    public void aciton() throws GitLabApiException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2020-01-01 00:00:00");
        Date end = sdf.parse("2020-01-13 23:59:59");

        List<Project> ps = Lists.emptyList();
        for (Project pro : ps) {
            List<Commit> commits = list(null, null, pro);

        }
    }

    public List<Commit> list(Date start, Date end, Project project) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("http://gitlab.top.mw", "N99pyaZem6sqbjvUPHSi");
        List<org.gitlab4j.api.models.Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), project.getBranch(), start, end);
        return null;
    }

}
