package com.example.Annotation;

import java.lang.annotation.*;

/// 将控制器方法的返回值直接写入 HTTP 响应体
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
