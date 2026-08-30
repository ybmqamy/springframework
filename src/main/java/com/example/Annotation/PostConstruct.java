package com.example.Annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/// 标明构造方法
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
