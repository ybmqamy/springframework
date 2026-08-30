package com.example.Annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/// 全部加 @Retention(RetentionPolicy.RUNTIME) 不然class运行的反射找不到
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    String initMethod();
    String destroyMethod();
}
