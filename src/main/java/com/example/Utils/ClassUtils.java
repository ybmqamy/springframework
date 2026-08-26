package com.example.Utils;

import com.example.Annotation.Component;
import com.example.Annotation.ComponentScan;
import com.example.Exception.NoInitOrDestoryMethodException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/// 解析class类的注解
public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        return clazz.getAnnotation(annotationType);
    }
    public static String getBeanName(Class<?> aClass){
        return aClass.getName();
    }
    public static String getBeanName(Method method){
        return method.getName();
    }
    public static <A extends Annotation> Method findAnnotationMethod(Class<?> clazz, Class<A> annotationType){
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if(declaredMethod.isAnnotationPresent(annotationType)){
                return declaredMethod;
            }
        }
        throw new NoInitOrDestoryMethodException("Aprilframework not find init or destory method");
    }
}
