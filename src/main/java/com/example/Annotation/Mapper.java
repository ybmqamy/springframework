package com.example.Annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/// mapper是mybatis层的，和他们不一样，现在是基于jdbc原生的数据库操作
/// 这里我先暂时把mapper层当spring的一部分
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Mapper {
    String value() default "";
}
