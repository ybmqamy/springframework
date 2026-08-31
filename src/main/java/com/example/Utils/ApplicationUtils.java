package com.example.Utils;

import com.example.Resolver.PropertyResolver;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import com.example.test.AppConfig;

import java.util.Map;
import java.util.Properties;

public class ApplicationUtils {
    public static ApplicationContext getRequiredApplicationContext() throws NoSuchMethodException {
        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
        Properties properties = new Properties();
        map.forEach((k, v) -> properties.put(k, String.valueOf(v)));
        PropertyResolver resolver = new PropertyResolver(properties);
        return new AnnotationConfigApplicationContext(AppConfig.class,resolver);
    };
}
