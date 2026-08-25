package com.example.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/// 创建bean的主体，包括各种bean的信息
/// 因为bean是ioc自己创建的，所以我们需要根据注解，获取对应类的构造方法，来构造这个对象
public class BeanDefinition {
    String name; ///名字

    Class<?> aClass; ///class对象

    Object instance=null; ///bean的实例，刚开始没有构造，为空

    Constructor<?> constructor; ///通过反射获取bean的构造方法

    String FactoryName; ///bean工厂的名字
    Method FactoryMethod;

    int order;   ///bean的排序

    boolean primary;  ///是否表明为primary，当有多个相同的bean，给那个设置高优先级
    /// 初始化的方法名字和销毁的名字
    String initName;
    String destoryName;
    /// 初始化的方法和销毁的方法
    Method initMethod;
    Method destoryMethod;
}
