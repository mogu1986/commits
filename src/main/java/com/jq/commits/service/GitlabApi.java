package com.jq.commits.service;

import com.google.common.base.Strings;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitlabApi {

    private static Map<String, String> names = new HashMap<String, String>() {{
        put("anxiaohu", "安小虎");
        put("zhangweizheng", "张维政");
        put("gaoying", "高颖");
        put("zhangshuguang", "张曙光");
        put("magic", "马国昌");
        put("lilinjie", "李林杰");
        put("jiguanglin", "吉广林");
    }};

    public static List<Commit> request(Date start, Date end, Integer projectId, String branch) throws GitLabApiException {
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
            if (line.startsWith("#") || Strings.isNullOrEmpty(line)) {
                continue;
            }

            Project project = new Project();

            String[] array = line.split(",");
            project.setGitlabUrl(array[0].trim());
            project.setName(array[1].trim());
            project.setBranch(array[2].trim());
            project.setId(Integer.valueOf(array[3].trim()));
            project.setDelete(Boolean.parseBoolean(array[4].trim()));

            data.add(project);
        }
        return data;
    }


    public static void execute(String startDate, String endDate) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = sdf.parse(startDate);
        Date end = sdf.parse(endDate);

        List<Project> pros = getProject();

        Connection con = JdbcUtils.getConnection();
        con.setAutoCommit(false);

        Statement stmt = null;

        List<Commit> commits = null;

        for (Project pro : pros) {

            // 1. 是否清空数据
            if (pro.isDelete()) {
                System.out.println("开始清空工程 : " + pro.getName() + " 数据");
                String sql = "delete from commits where project_id = " + pro.getId();
                System.out.println(sql);

                stmt = con.createStatement();
                stmt.executeUpdate(sql);
                con.commit();
                System.out.println("结束清空工程 : " + pro.getName() + " 数据\n");
            }

            // 2. 拿到commit数据
            System.out.println("开始获取" + pro.getName() + " commits 记录");
            commits = request(start, end, pro.getId(), pro.getBranch());
            System.out.println("结束获取" + pro.getName() + " commits 记录, 一共" + commits.size() + "次提交\n");

            // 3. 开始批量插入
            if (!CollectionUtils.isEmpty(commits)) {
                insert(pro, commits, con);
            }
        }

        // 4. 替换名字
        replace(con, stmt);

        JdbcUtils.close(stmt, con);
    }

    private static void replace(Connection con, Statement stmt) {
        for (Map.Entry<String, String> n : names.entrySet()) {
            String sql = "update commits set name = '" + n.getValue() + "' where name = '" + n.getKey() + "'";
            try {
                stmt.executeUpdate(sql);
                con.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void insert(Project pro, List<Commit> commits, Connection con) throws Exception {

        List<List<Commit>> parts = Lists.partition(commits, 10000);

        String sql = "insert into commits(name,project,additions,deletions,committed_date,commit_id,project_id) values (?,?,?,?,?,?,?)";
        PreparedStatement pstmt = con.prepareStatement(sql);

        for (List<Commit> list : parts) {
            for (Commit e : list) {
                pstmt.setString(1, e.getAuthorName());
                pstmt.setString(2, pro.getName());
                pstmt.setInt(3, e.getStats().getAdditions());
                pstmt.setInt(4, e.getStats().getDeletions());
                pstmt.setTimestamp(5, new java.sql.Timestamp(e.getCommittedDate().getTime()));
                pstmt.setString(6, e.getId());
                pstmt.setInt(7, pro.getId());

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            con.commit();
        }
        pstmt.close();
    }

    public static void main(String[] args) throws Exception {
        long startMili = System.currentTimeMillis();
        execute("2021-01-01 00:00:00", "2021-01-13 23:59:59");
        long endMili = System.currentTimeMillis();
        System.out.println("/**总耗时为：" + (endMili - startMili)/1000 + "秒");
    }

}
