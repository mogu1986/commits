package com.jq.commits.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Commit implements Serializable {

    private Integer id;

    private String name;

    private Integer additions;

    private Integer deletions;

    private Integer total;

    private Date committedDate;

    private String commitUrl;

}
