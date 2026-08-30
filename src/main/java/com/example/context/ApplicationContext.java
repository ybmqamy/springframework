package com.example.context;


import java.util.List;

public interface ApplicationContext extends AutoCloseable {

    // 是否存在指定name的Bean？
    boolean containsBean(String name);

    // 根据name返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
    <T> T findBean(String name);


    // 根据type返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
    <T> T findBean(Class<T> requiredType);

    // 根据type返回一组Bean，未找到返回空List
    <T> List<T> findBeans(Class<T> requiredType);

    // 关闭并执行所有bean的destroy方法
    void close();
}
