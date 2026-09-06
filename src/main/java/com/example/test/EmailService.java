package com.example.test;

import com.example.Annotation.*;
import com.example.Jdbc.JdbcTemplate;
import com.example.Jdbc.RowMapper;

import java.util.List;

/// @Service + @Primary + @Order：多个同类型 bean 时优先选它
/// email和sms同时实现了messageservie方法，谁加primary，就先选谁
@Service
@Primary
@Order(value = 1)
@Transactional
public class EmailService implements MessageService {
    @Autowired
    JdbcTemplate jdbcTemplate;

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

    public <T> List<T> getaccount(String sql, RowMapper<T> rowMapper, Object ...args){
        return jdbcTemplate.queryForList(sql,rowMapper,args);
    }

    public int  insert(String sql,Object...args){
        return jdbcTemplate.insert(sql,args);
    }

}
