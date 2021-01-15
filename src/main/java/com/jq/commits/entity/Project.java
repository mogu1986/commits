package com.jq.commits.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Project implements Serializable {

    private Integer id;

    private String name;

    private String gitlabUrl;

    private String branch;

    private boolean delete;

}
