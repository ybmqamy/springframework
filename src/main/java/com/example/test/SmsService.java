package com.example.test;

import com.example.Annotation.Order;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Service;
import com.example.Annotation.Value;

/// 与 EmailService 同实现 MessageService，但不标 @Primary，用来验证 @Primary 的选择逻辑
@Service
@Order(value = 2)
public class SmsService implements MessageService {

    @Value("app.title")
    private String title;

    @PostConstruct
    public void init() {
        System.out.println("[SmsService] @PostConstruct: title=" + title);
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("[SmsService] @PreDestroy");
    }

    @Override
    public String getMessage() {
        return "SMS: " + title;
    }
}
