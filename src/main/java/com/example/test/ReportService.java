package com.example.test;

/// 普通类，不加任何组件注解，只通过 @Bean 工厂方法创建
/// init()/destroy() 配合 @Bean 的 initMethod/destroyMethod 调用
public class ReportService {

    public void init() {
        System.out.println("[ReportService] @Bean initMethod 被调用");
    }

    public void destroy() {
        System.out.println("[ReportService] @Bean destroyMethod 被调用");
    }
}
