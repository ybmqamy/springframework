package com.example.test;

import com.example.Annotation.*;

@Component
@Transactional("NoPoliteInvocationHandler")
public class Bob {
    public String name;

    @testannotation
    public String hello() {
        return "Hello, " + name + ".";
    }

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
