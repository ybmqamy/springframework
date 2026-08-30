package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Value;

@Component
public class OriginBean {
    @Value("${app.title}")
    public String name;

    @Value("${app.version}")
    public String version;
    @PostConstruct
    public void init() {
        System.out.println("[ReportService] @Bean initMethod 被调用");
    }
    @PreDestroy
    public void destroy() {
        System.out.println("[ReportService] @Bean destroyMethod 被调用");
    }
    public String getName() {
        return name;
    }
}