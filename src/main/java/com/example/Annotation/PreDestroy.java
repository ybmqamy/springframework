package com.example.Annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/// 标明销毁的方法
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
