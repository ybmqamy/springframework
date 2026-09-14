package com.example.webmvc;

import com.example.Resolver.PropertyResolver;
import com.example.Servlet.DispatcherServlet;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.webmvc.app.TestConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/// 这里都没有参与打包，也是根本不可能找得到
///
public class WebMvcTest {

    static AnnotationConfigApplicationContext ctx;
    static DispatcherServlet servlet;

    @BeforeAll
    static void setUp() throws Exception {
        PropertyResolver resolver = new PropertyResolver(new Properties());
        ctx = new AnnotationConfigApplicationContext(TestConfig.class, resolver);
        servlet = new DispatcherServlet(ctx, resolver);
        servlet.init();
    }

    @AfterAll
    static void tearDown() {
        ctx.close();
    }

    @Test
    void getHello() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/api/hello");
        /// 测试直接返回值
        StringWriter body = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.service(req, resp);
        /// 检验返回的是否相等 ,不相等会报错的
        assertEquals("Hello WebMvc", body.toString());
        verify(resp).setContentType("application/json");
    }

    @Test
    void getSumWithRequestParam() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/api/sum");
        when(req.getParameter("a")).thenReturn("10");
        when(req.getParameter("b")).thenReturn("32");
        /// 设置路径参数
        StringWriter body = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.service(req, resp);

        assertEquals("sum:42", body.toString());
    }
    /// 测试请求体
    @Test
    void postEchoWithRequestBody() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURI()).thenReturn("/api/echo");  ///构造了个json参数
        when(req.getReader()).thenReturn(new BufferedReader(new StringReader("{\"message\":\"hi\"}")));

        StringWriter body = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        servlet.service(req, resp);

        assertEquals("echo:hi", body.toString());
    }

    @Test
    void unknownUrlReturns404() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURI()).thenReturn("/api/not-exist");

        servlet.service(req, resp);

        verify(resp).sendError(404, "Not Found");
    }
}
