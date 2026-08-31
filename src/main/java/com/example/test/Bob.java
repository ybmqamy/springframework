package com.example.test;

import com.example.Annotation.*;

@Component
@Around("PoliteInvocationHandler")
public class Bob {
    public String name;

    public Bob() {
    }

    public Bob(String name) {
        this.name = name;
    }

    @testannotation
    public String hello() {
        return "Hello, " + name + ".";
    }
    @PostConstruct
    void init(){}
    @PreDestroy
    void destro(){};

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "Bob{name = " + name + "}";
    }
}
