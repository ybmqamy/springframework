package com.example.Listener;

import com.example.Exception.BeanCreationException;
import com.example.Exception.NestedRuntimeException;
import com.example.Resolver.PropertyResolver;
import com.example.Servlet.DispatcherServlet;
import com.example.Utils.WebUtils;
import com.example.Utils.YamlUtils;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import com.example.test.AppConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Map;
import java.util.Properties;

import static com.example.Utils.ApplicationUtils.applicationContext;

/// listener 是最先被创建的，它负责监听servlet的状态，并且完成ioc的装配和servlet的注册
public class ContextLoaderListener implements ServletContextListener{
    /// 在原生的spring中，就是先创建的listener，然后调用上下文初始化器，完成ioc容器的初始化
    @Override
    public void contextInitialized(ServletContextEvent sce){
        ServletContext servletContext = sce.getServletContext();
        /// 拿到对应的配置解析器
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProper("${app.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        /// 设置请求和回送的解码格式
        try {
            ApplicationContext application = createApplicationContext(servletContext.getInitParameter("configuration"), propertyResolver);
            WebUtils.registerDispatcherServlet(servletContext, propertyResolver);
            servletContext.setAttribute("applicationContext",application);
        } catch (NestedRuntimeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) throws NestedRuntimeException, NoSuchMethodException {
        if (configClassName == null || configClassName.isEmpty()) {
            throw new NestedRuntimeException("Cannot init ApplicationContext for missing init param name: configuration");
        }
        /// 这里已经做了类名的转换，很成功
        Class<?> configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new NestedRuntimeException("Could not load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplicationContext(configClass, propertyResolver);
    }
}
