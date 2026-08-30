package com.example.Annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/// 应该优先展示哪个bean
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
