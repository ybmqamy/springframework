package com.example.Utils;

import com.example.Exception.ApplicationCreateException;
import com.example.Resolver.PropertyResolver;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import com.example.test.AppConfig;

import java.util.Map;
import java.util.Properties;

public class ApplicationUtils {
    private static ApplicationContext applicationContext;
    /// 设置applicationcontest，复用已有的context
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    public static ApplicationContext getRequiredApplicationContext() throws NoSuchMethodException {
        if(applicationContext==null){
            throw new ApplicationCreateException("当前主容器尚未初始化");
        }
        return applicationContext;
    };
}
