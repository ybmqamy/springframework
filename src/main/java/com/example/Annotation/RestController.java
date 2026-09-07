package com.example.Annotation;

import java.lang.annotation.*;

/// 返回都写进返回体，作为json格式返回，
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RestController {
}
