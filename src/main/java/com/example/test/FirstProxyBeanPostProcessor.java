package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.Order;
import com.example.Annotation.PostConstruct;
import com.example.Annotation.PreDestroy;
import com.example.Bean.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;

@Order(100)
@Component
public class FirstProxyBeanPostProcessor implements BeanPostProcessor {
    // 保存原始Bean:
    Map<String, Object> originBeans = new HashMap<>();
    @PostConstruct
    public void init() {
        System.out.println("[ReportService] @Bean initMethod 被调用");
    }
    @PreDestroy
    public void destroy() {
        System.out.println("[ReportService] @Bean destroyMethod 被调用");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (OriginBean.class.isAssignableFrom(bean.getClass())) {
            // 检测到OriginBean,创建FirstProxyBean:
            var proxy = new FirstProxyBean((OriginBean) bean);
            // 保存原始Bean:
            originBeans.put(beanName, bean);
            // 返回Proxy:
            return proxy;
        }
        return bean;
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originBeans.get(beanName);
        if (origin != null) {
            // 存在原始Bean时,返回原始Bean:
            return origin;
        }
        return bean;
    }
}

// 代理Bean:
class FirstProxyBean extends OriginBean {
    final OriginBean target;
    @PostConstruct
    public void init() {
        System.out.println("[ReportService] @Bean initMethod 被调用");
    }
    @PreDestroy
    public void destroy() {
        System.out.println("[ReportService] @Bean destroyMethod 被调用");
    }

    public FirstProxyBean(OriginBean target) {
        this.target = target;
    }
}