package com.example.Dispatcher;

import com.example.Annotation.ResponseBody;
import com.example.Exception.ServerErrorException;
import com.example.Exception.ServerWebInputException;
import com.example.Utils.JsonUtils;
import com.example.Utils.PathUtils;
import com.example.Utils.WebUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dispatcher {
    // 是否返回REST:
    public Boolean isRest;
    // 是否有@ResponseBody:
    public boolean isResponseBody;
    // 是否返回void:
    public boolean isVoid;
    // URL正则匹配:
    public Pattern urlPattern;
    // Bean实例:
    public Object controller;
    // 处理方法:
    public Method handlerMethod;
    // 方法参数:
    public Param[] methodParameters;

    public Dispatcher(String httpMethod, boolean isRest, Object controller, Method method, String urlPattern) throws ServletException {
        this.isRest = isRest;
        this.isResponseBody = method.getAnnotation(ResponseBody.class) != null;
        this.isVoid = method.getReturnType() == void.class;
        this.urlPattern = PathUtils.compile(urlPattern);
        this.controller = controller;
        this.handlerMethod = method;
        Parameter[] params = method.getParameters();
        Annotation[][] paramsAnnos = method.getParameterAnnotations();
        this.methodParameters = new Param[params.length];
        for (int i = 0; i < params.length; i++) {
            this.methodParameters[i] = new Param(httpMethod, method, params[i], paramsAnnos[i]);
        }
    }
    /// yield得到返回值
    /// 对controller的调用在这里就完成了
    public Result process(String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Matcher matcher = urlPattern.matcher(url);
        ///这个地方和传入进来的url参数进行匹配，只有完全相同才会读取参数
        if (matcher.matches()) {
            Object[] arguments = new Object[this.methodParameters.length];
            /// 解析每一个参数
            for (int i = 0; i < arguments.length; i++) {
                Param param = methodParameters[i];
                /// 根据参数的类型，来做处理，最后返回result结果树
                arguments[i] = switch (param.paramType) {
                    case PATH_VARIABLE -> {  /// 处理路径参数,这个参数在url中，
                        try {     /// 返回指定分组的匹配内容并转换为包装类
                            String s = matcher.group(param.name);
                            yield convertToType(param.classType, s);
                        } catch (IllegalArgumentException e) {
                            throw new ServerWebInputException("Path variable '" + param.name + "' not found.");
                        }
                    }
                    case REQUEST_BODY -> {   /// json对象，从请求体中读取json对象
                        BufferedReader reader = request.getReader();       ///这里完成读取json
                        yield JsonUtils.readJson(reader, param.classType);
                    }
                    case REQUEST_PARAM -> {   /// url参数处理表单的参数
                        String s = getOrDefault(request, param.name, param.defaultValue);
                        yield convertToType(param.classType, s);
                    }
                    case SERVLET_VARIABLE -> {  /// 处理其他类型
                        Class<?> classType = param.classType;
                        if (classType == HttpServletRequest.class) {
                            yield request;
                        } else if (classType == HttpServletResponse.class) {
                            yield response;
                        } else if (classType == HttpSession.class) {
                            yield request.getSession();
                        } else if (classType == ServletContext.class) {
                            yield request.getServletContext();
                        } else {
                            throw new ServerErrorException("Could not determine argument type: " + classType);
                        }
                    }
                    case NOT_PROCESSED ->  null;
                };
            }
            Object result = null;
            try {  /// 填入参数，调用处理方法,这里拿到的就是 返回的数据，后续需要定义该怎么返回
                result = this.handlerMethod.invoke(this.controller, arguments);
            } catch (Exception e) {
                throw new RuntimeException("调用controller异常");
            }           ///这里直接拿到它的返回结果
            return new Result(true,200,result);
        }
        return null;
    }
    /// 完成普通类到到包装类的转变
    Object convertToType(Class<?> classType, String s) {
        if (classType == String.class) {
            return s;
        } else if (classType == boolean.class || classType == Boolean.class) {
            return Boolean.valueOf(s);
        } else if (classType == int.class || classType == Integer.class) {
            return Integer.valueOf(s);
        } else if (classType == long.class || classType == Long.class) {
            return Long.valueOf(s);
        } else if (classType == byte.class || classType == Byte.class) {
            return Byte.valueOf(s);
        } else if (classType == short.class || classType == Short.class) {
            return Short.valueOf(s);
        } else if (classType == float.class || classType == Float.class) {
            return Float.valueOf(s);
        } else if (classType == double.class || classType == Double.class) {
            return Double.valueOf(s);
        } else {
            throw new ServerErrorException("Could not determine argument type: " + classType);
        }
    }

    String getOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String s = request.getParameter(name);
        if (s == null) {
            if (WebUtils.DEFAULT_PARAM_VALUE.equals(defaultValue)) {
                throw new ServerWebInputException("Request parameter '" + name + "' not found.");
            }
            return defaultValue;
        }
        return s;
    }
}
