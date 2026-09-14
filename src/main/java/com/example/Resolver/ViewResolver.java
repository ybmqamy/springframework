package com.example.Resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
/// 渲染器，解析返回数据为页面的请求
public interface ViewResolver {
    // 初始化ViewResolver:
    void init();

    // 渲染:
    void render(String viewName, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}