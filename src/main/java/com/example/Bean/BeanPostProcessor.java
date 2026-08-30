package com.example.Bean;

import javax.annotation.Nullable;
///  beanpostprocessor的基本接口，我们实际上不需要做什么
/// 对应的bean如果想要去做类型的转换，那么让对应的类继承这个接口，然后实现这个接口即可
/// applicationcontext只需要检测对应的类是否实现了该方法即可
/// BeanPostProcessor自身也作为一个bean返回，所以初始化完成，找到所有有这个的，直接转换即可
/// postProcessOnSetProperty保存原始的bean，并不是所有的实例都要替换的
public interface BeanPostProcessor {
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }

    @Nullable
    default Object postProcessOnSetProperty(Object bean, String beanName) {
        return bean;
    }
}