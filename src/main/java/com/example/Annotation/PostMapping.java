package com.example.Annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface PostMapping {
    String value() default "";
}
