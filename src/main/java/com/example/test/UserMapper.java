package com.example.test;

import com.example.Annotation.Mapper;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Value;

@Mapper
public class UserMapper {

    @Value("app.datasource.url")
    private String url;

    @Value("app.datasource.username")
    private String username;

    @PostConstruct
    public void init() {
        System.out.println("[UserMapper] @PostConstruct: url=" + url + ", username=" + username);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[UserMapper] @PreDestroy");
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserMapper{url='" + url + "', username='" + username + "'}";
    }
}
