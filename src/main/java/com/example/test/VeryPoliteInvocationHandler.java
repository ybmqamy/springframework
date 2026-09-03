package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.Configuration;
import com.example.Annotation.testannotation;
import com.example.Invocation.BeforeInvocationHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class VeryPoliteInvocationHandler extends BeforeInvocationHandlerAdapter {
    @Override
    public Object before(Object proxy, Method method, Object[] args) throws InvocationTargetException {
        if(method.getAnnotation(testannotation.class)!=null) {
            String invoke = null;
            try {
                invoke = (String) method.invoke(proxy, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            String s = invoke + "adadadadacacca";
            System.out.println("我是"+this.getClass());
            return s;
        }
        try {
            return method.invoke(proxy,args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
