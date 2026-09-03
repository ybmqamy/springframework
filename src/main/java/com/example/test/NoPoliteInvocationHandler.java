package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.Configuration;
import com.example.Annotation.testannotation;
import com.example.Invocation.AfterInvocationHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class NoPoliteInvocationHandler extends AfterInvocationHandlerAdapter {
    @Override
    public Object after(Object proxy, Object returnValue, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if(method.getAnnotation(testannotation.class)!=null) {
            String invoke =(String) method.invoke(proxy, args);
            String s = invoke + "adadadadacacca";
            System.out.println("我是"+this.getClass());
            return s;
        }
        return method.invoke(proxy,args);
    }
}
