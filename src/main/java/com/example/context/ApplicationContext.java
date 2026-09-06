package com.example.context;


import com.example.Bean.BeanDefinition;

import java.util.List;

/// 对外暴露的容器门面：只提供"按名字/类型取 bean 实例"与生命周期能力，
/// BeanDefinition 等内部细节由具体实现负责，避免调用方直接触碰容器内部结构。
public interface ApplicationContext extends AutoCloseable {

    // 是否存在指定name的Bean定义？
    boolean containsBean(String name);

    // 根据name返回唯一的Bean实例，未找到抛出NoSuchBeanDefinitionException
    <T> T getBean(String name);

    // 根据type返回唯一的Bean实例，未找到抛出NoSuchBeanDefinitionException，存在多个且无@Primary时抛出NoUniqueBeanDefinitionException
    <T> T getBean(Class<T> requiredType);

    // 根据type返回一组Bean实例，未找到返回空List
    <T> List<T> getBeans(Class<T> requiredType);

    // 根据name返回BeanDefinition，供框架内部与BeanPostProcessor使用
    BeanDefinition getBeanDefinition(String name);

    // 关闭并执行所有bean的destroy方法
    void close();
}
