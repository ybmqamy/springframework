package com.example.Utils;

import com.example.Annotation.Component;
import com.example.Annotation.ComponentScan;
import com.example.Annotation.Value;
import com.example.Exception.NoInitOrDestoryMethodException;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/// 解析class类的注解
public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        return clazz.getAnnotation(annotationType);
    }
    public static String getBeanName(Class<?> aClass){
        /// 这里字符串截取，直接返回类名
        return  aClass.getSimpleName();
    }
    public static String getBeanName(Method method){
        /// method.getName() 这是根据方法的名字找的
        return method.getName();
    }
    public static <A extends Annotation> Method findAnnotationMethod(Class<?> clazz, Class<A> annotationType){
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if(declaredMethod.isAnnotationPresent(annotationType)){
                return declaredMethod;
            }
        }
        return null;
        /// 这里用在了找pre方法上，但这个不应该是强制性的要求
    }
    public static <A extends Annotation> A findAnnotationParameter(Annotation[] annotations, Class<A> annotationType) {
        if (annotations == null || annotationType == null) {
            return null;
        }
        for (Annotation annotation : annotations) {
            /// 判断当前注解是否是目标类型的实例
            if (annotationType.isInstance(annotation)) {
                /// 安全地转换为目标类型并返回
                return annotationType.cast(annotation);
            }
        }
        // 没有找到匹配的注解
        return null;
    }
    @Nullable
    public static Method getnamemethod(Class<?> clazz,String namemethod){
        try {
            return clazz.getMethod(namemethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
