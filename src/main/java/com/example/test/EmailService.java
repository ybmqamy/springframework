package com.example.test;

import com.example.Annotation.Order;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Primary;
import com.example.Annotation.Service;
import com.example.Annotation.Value;

/// @Service + @Primary + @Order：多个同类型 bean 时优先选它
/// email和sms同时实现了messageservie方法，谁加primary，就先选谁
@Service
@Primary
@Order(value = 1)
public class EmailService implements MessageService {

    @Value("app.title")
    private String title;

    @Value("app.author")
    private String author;

    @PostConstruct
    public void init() {
        System.out.println("[EmailService] @PostConstruct: title=" + title + ", author=" + author);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[EmailService] @PreDestroy");
    }

    @Override
    public String getMessage() {
        return "Email from " + author + ": " + title;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
