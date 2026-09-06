package com.example.Annotation;

import java.lang.annotation.*;

/// 本spring仅仅支持jdbc本地事务和事务传播模型仅支持最常用的REQUIRED
/// REQUIRED如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新事务。 spring也是使用次默认
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited  /// 标记可以被子类自动继承
@Documented /// 标记一个注解在使用时应当被 Javadoc 工具记录到生成的 API 文档中。
public @interface Transactional {
    String value() default "platformTransactionManager";
    /// 默认用名字为PlatformTransactionManager的bean来管理事务
}
