package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Bean.BeanPostProcessor;

/// 实现 BeanPostProcessor，覆盖三个扩展点，打印日志方便观察生命周期
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {

    @PostConstruct
    public void init() {
        System.out.println("[LoggingBeanPostProcessor] @PostConstruct");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("[LoggingBeanPostProcessor] @PreDestroy");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("[LoggingBeanPostProcessor] postProcessBeforeInitialization: " + beanName + " -> " + bean);
        return bean;
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        System.out.println("[LoggingBeanPostProcessor] postProcessOnSetProperty: " + beanName + " -> " + bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("[LoggingBeanPostProcessor] postProcessAfterInitialization: " + beanName + " -> " + bean);
        return bean;
    }
}
