package com.example.test;

import com.example.Annotation.Autowired;
import com.example.Annotation.Controller;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Annotation.Value;

@Controller
public class UserController {

    // 字段注入：接口类型，验证 @Primary 选择 EmailService
    @Autowired(value = true)
    private MessageService messageService;

    // 字段注入：int 类型转换
    @Value("server.port")
    private int port;

    // 字段注入：int 类型转换
    @Value("app.timeout")
    private int timeout;

    // 方法注入：通过 setter 注入 UserMapper
    private UserMapper mapper;

    @Autowired(value = true)
    public void setMapper(UserMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        System.out.println("[UserController] @PostConstruct: messageService=" + messageService
                + ", port=" + port + ", timeout=" + timeout + ", mapper=" + mapper);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[UserController] @PreDestroy");
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public UserMapper getMapper() {
        return mapper;
    }
}
