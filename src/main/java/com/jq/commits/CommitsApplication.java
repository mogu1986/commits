package com.jq.commits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class CommitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommitsApplication.class, args);
    }

    @GetMapping("/index")
    public String mm() {
        return "index";
    }

}
