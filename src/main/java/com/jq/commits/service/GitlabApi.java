package com.jq.commits.service;

import com.google.common.collect.Lists;
import com.jq.commits.entity.Project;
import com.jq.commits.utils.JdbcUtils;
import org.apache.commons.io.IOUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GitlabApi {


    public static List<Commit> action(Date start, Date end, Integer projectId, String branch) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("http://gitlab.top.mw", "N99pyaZem6sqbjvUPHSi");
        List<Commit> commits = gitLabApi.getCommitsApi().getCommits(projectId, branch, start, end);

        List<Commit> data = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(commits)) {
            for (Commit commit : commits) {
                Commit row = gitLabApi.getCommitsApi().getCommit(projectId, commit.getShortId());

                data.add(row);
            }
        }

        return data;
    }

    public static List<Project> getProject() {
        ClassPathResource cpr = new ClassPathResource("project.txt");
        List<String> txt = null;
        try {
            txt = IOUtils.readLines(cpr.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<Project> data = Lists.newArrayList();

        for (String line : txt) {
            if (line.startsWith("#")) {
                continue;
            }

            Project project = new Project();

            String[] array = line.split(",");
            project.setGitlabUrl(array[0]);
            project.setName(array[1]);
            project.setBranch(array[2]);
            project.setId(Integer.valueOf(array[3]));
            project.setDelete(Boolean.parseBoolean(array[4]));

            data.add(project);
        }
        return data;
    }


    public static void main(String[] args) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse("2021-01-01 00:00:00");
        Date end = sdf.parse("2021-01-13 23:59:59");

        List<Project> pros = getProject();

        Connection con = JdbcUtils.getConnection();
        con.setAutoCommit(false);

        Statement stmt = null;

        List<Commit> commits = null;

        for (Project pro : pros) {

            // 事先清空数据
            if (pro.isDelete()) {
                String sql = "delete from commits where project_id = " + pro.getId();
                System.out.println(sql);

                stmt = con.createStatement();
                stmt.executeUpdate(sql);
                con.commit();
            }

            // 拿到数据
            commits = action(start, end, pro.getId(), pro.getBranch());

            System.out.println(commits.size());

            if (!CollectionUtils.isEmpty(commits)) {
                insert(pro, commits, con);
            }

        }

        // 开始批量插入
        JdbcUtils.close(stmt, con);
    }

    private static void insert(Project pro, List<Commit> commits,Connection con) throws Exception {

        List<List<Commit>> parts = Lists.partition(commits, 10);

        System.out.println("分割后:" + parts.size());

        String sql = "insert into commits(name,project,additions,deletions,committed_date,commit_id,project_id) values (?,?,?,?,?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(sql);

        for (List<Commit> list : parts) {
            for (Commit e : list) {
                pstmt.setString(1, e.getAuthorName());
                pstmt.setString(2, pro.getName());
                pstmt.setInt(3, e.getStats().getAdditions());
                pstmt.setInt(4, e.getStats().getDeletions());
                pstmt.setDate(5, new java.sql.Date(e.getAuthoredDate().getTime()));
                pstmt.setString(6, e.getId());
                pstmt.setInt(7, pro.getId());

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            con.commit();
        }
        pstmt.close();

    }

}
