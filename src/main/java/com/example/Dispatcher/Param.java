package com.example.Dispatcher;

import com.example.Annotation.PathVariable;
import com.example.Annotation.RequestBody;
import com.example.Annotation.RequestParam;
import com.example.Exception.ServerErrorException;
import com.example.Utils.ClassUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

class Param {
    // 参数名称:
    String name;
    // 参数类型:
    ParamType paramType;
    // 参数Class类型:
    Class<?> classType;
    // 参数默认值
    String defaultValue;
    /// 根据对应的类型设置参数的类型
    public Param(String httpMethod, Method method, Parameter parameter, Annotation[] annotations) throws ServletException {
        PathVariable pv = ClassUtils.findAnnotationParameter(annotations, PathVariable.class);  ///拿到路径参数
        RequestParam rp = ClassUtils.findAnnotationParameter(annotations,RequestParam.class);   ///拿到name=？？？的参数
        RequestBody rb = ClassUtils.findAnnotationParameter(annotations, RequestBody.class);    ///拿到json参数
        // should only have 1 annotation:
        int total = (pv == null ? 0 : 1) + (rp == null ? 0 : 1) + (rb == null ? 0 : 1);
        if (total > 1) {
            throw new ServletException("Annotation @PathVariable, @RequestParam and @RequestBody cannot be combined at method: " + method);
        }
        this.classType = parameter.getType();
        /// 设置路径参数,路径参数需要从请求体中取得值，所以需要value
        if (pv != null) {
            this.name = pv.value();
            this.paramType = ParamType.PATH_VARIABLE;
        } else if (rp != null) {   /// 设置表单参数，表单既有value，还会有默认值（空）
            this.name = rp.value();
            this.defaultValue = rp.defaultValue();
            this.paramType = ParamType.REQUEST_PARAM;
        } else if (rb != null) {  /// json 啥也不需要
            this.paramType = ParamType.REQUEST_BODY;
        } else {
            this.paramType = ParamType.SERVLET_VARIABLE;
            // check servlet variable type:
            if (this.classType != HttpServletRequest.class && this.classType != HttpServletResponse.class && this.classType != HttpSession.class
                    && this.classType != ServletContext.class) {
                throw new ServerErrorException("(Missing annotation?) Unsupported argument type: " + classType + " at method: " + method);
            }  ///如果也不是servlet的参数，那么就是错了
        }
    }
}