package com.example.Dispatcher;

public enum ParamType {
    PATH_VARIABLE, //路径参数，从URL中提取；
    REQUEST_PARAM, //URL参数，从URL Query或Form表单提取；
    REQUEST_BODY, //：REST请求参数，从Post传递的JSON提取；
    SERVLET_VARIABLE, //：HttpServletRequest等Servlet API提供的参数，直接从DispatcherServlet的方法参数获得。
    NOT_PROCESSED
}
