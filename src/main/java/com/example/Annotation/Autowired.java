package com.example.Annotation;

public @interface Autowired {
    String name() default "";
    boolean value();
}
