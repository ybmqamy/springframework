package com.example.Utils;

import com.example.Resolver.PropertyResolver;
import com.example.Servlet.DispatcherServlet;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import jakarta.servlet.ServletContext;

import java.util.Map;
import java.util.Properties;

public class WebUtils {
    public static final String DEFAULT_PARAM_VALUE = "";

    public static PropertyResolver createPropertyResolver(){
        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
        Properties properties = new Properties();
        map.forEach((k, v) -> properties.put(k, String.valueOf(v)));
        PropertyResolver resolver = new PropertyResolver(properties);
        return resolver;
    }
    /// 登记为servlet
    /// listener启动后，注册ioc容器，再接着就是创建servlet
    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver , ApplicationContext applicationContext) {
        ///todo这里登记servlet，已经把dispatcherservlet整合进去了
        var dispatcherServlet = new DispatcherServlet((AnnotationConfigApplicationContext) applicationContext,null);
        // 这里已经把dis注册到servletcontext当中
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");  //接管所有请求
        dispatcherReg.setLoadOnStartup(0);   //设置servlet为开始立即启动
    }
}
