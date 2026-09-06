package com.example.Utils;

import com.example.Resolver.PropertyResolver;
import com.example.Servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;

import java.util.Map;
import java.util.Properties;

public class WebUtils {
    public static PropertyResolver createPropertyResolver(){
        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
        Properties properties = new Properties();
        map.forEach((k, v) -> properties.put(k, String.valueOf(v)));
        PropertyResolver resolver = new PropertyResolver(properties);
        return resolver;
    }
    /// 登记为servlet
    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver) {
        var dispatcherServlet = new DispatcherServlet();
        // 注册DispatcherServlet:
        var dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");  //接管所有请求
        dispatcherReg.setLoadOnStartup(0);   //设置servlet为开始立即启动
    }
}
