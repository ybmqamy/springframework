package com.example.test;

import com.example.Annotation.Bean;
import com.example.Annotation.Component;
import com.example.Annotation.ComponentScan;
import com.example.Annotation.Configuration;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;

/// 配置类：需要同时标 @Configuration 和 @Component 才会被扫描创建
@Configuration
@Component
@ComponentScan("com.example.test")
public class AppConfig {

    @PostConstruct
    public void init() {
        System.out.println("[AppConfig] @PostConstruct");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[AppConfig] @PreDestroy");
    }
    /// 这里通过工厂方法创建的bean
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public ReportService reportService() {
        System.out.println("[AppConfig] 通过 @Bean 工厂方法创建 reportService");
        return new ReportService();
    }
}
