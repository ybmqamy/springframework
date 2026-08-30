package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Value;

/// 构造方法注入：通过构造参数 @Value 注入（含 boolean 类型转换）
@Component
public class DataSourceComponent {

    private final String password;
    private final boolean enabled;

    public DataSourceComponent(
            @Value("app.datasource.password") String password,
            @Value("app.enabled") boolean enabled) {
        this.password = password;
        this.enabled = enabled;
    }

    @PostConstruct
    public void init() {
        System.out.println("[DataSourceComponent] @PostConstruct: password=" + password + ", enabled=" + enabled);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[DataSourceComponent] @PreDestroy");
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
