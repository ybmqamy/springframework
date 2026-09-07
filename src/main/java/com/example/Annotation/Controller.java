package com.example.Annotation;

import java.lang.annotation.*;

/// 返回原始的html页面
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
@Target(ElementType.TYPE)
public @interface Controller {
    String value() default "";
}
