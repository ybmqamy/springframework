package com.example.test;

import com.example.Annotation.*;
import com.example.Bean.AroundProxyBeanPostProcessor;
import com.example.Bean.TransactionalProxyBeanPostProcessor;
import com.example.Jdbc.JdbcTemplate;

import javax.sql.DataSource;

/// 配置类：需要同时标 @Configuration 和 @Component 才会被扫描创建
/// 这是我ioc启动的门户
@Configuration
@Component
@ComponentScan({"com.example.test","com.example.Configuration","com.example.Manager"})
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

    /// 注册拦截器成为bean
    /// 这里拦截器已经注册为bean了，所以在创建的时候，也可以创建这个对应的实例
    /// 那我定义的around那个玩意也一定会生效
    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }
}
