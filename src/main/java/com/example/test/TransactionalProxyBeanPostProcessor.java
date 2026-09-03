package com.example.test;

import com.example.Annotation.Transactional;
import com.example.Bean.AnnotationProxyBeanPostProcessor;
/// 这个类就会继承所有它的方法
public class TransactionalProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {
}
