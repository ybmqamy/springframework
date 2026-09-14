package com.example.test;

import com.example.Annotation.Autowired;
import com.example.Annotation.Bean;
import com.example.Annotation.Configuration;
import com.example.Annotation.Value;
import com.example.Marker.FreeMarkerViewResolver;
import com.example.Resolver.ViewResolver;
import jakarta.servlet.ServletContext;

import java.util.Objects;

@Configuration
public class WebMvcConfiguration {
    private static ServletContext servletContext = null;
    static void setServletContext(ServletContext ctx) {
        servletContext = ctx;
    }
    /// 负责注册viewresolver
    @Bean(initMethod = "init")
    ViewResolver viewResolver( //
                               @Autowired ServletContext servletContext, //
                               @Value("${summer.web.freemarker.template-path:/WEB-INF/templates}") String templatePath, //
                               @Value("${summer.web.freemarker.template-encoding:UTF-8}") String templateEncoding) {
        return new FreeMarkerViewResolver(servletContext, templatePath, templateEncoding);
    }
    /// 注册servletcontext
    @Bean
    ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
        /// 有这个就返回，没有就就报错,
    }
}