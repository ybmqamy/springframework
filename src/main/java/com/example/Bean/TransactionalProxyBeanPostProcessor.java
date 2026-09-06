package com.example.Bean;

import com.example.Annotation.Transactional;
/// 由JdbcConfiguration创建的DataSource，实现了连接池；
/// 由JdbcConfiguration创建的JdbcTemplate，实现基本SQL操作；
/// 由JdbcConfiguration创建的PlatformTransactionManager，负责拦截@Transactional标识的Bean的public方法，自动管理事务；
/// 由JdbcConfiguration创建的TransactionalBeanPostProcessor，
/// 负责给@Transactional标识的Bean创建AOP代理，拦截器正是PlatformTransactionManager。
/// 这个类就会继承所有它的方法
public class TransactionalProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {
}
