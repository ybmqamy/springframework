package com.example.test;

import com.example.Annotation.Component;
import com.example.Annotation.testannotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Component
public class PoliteInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getAnnotation(testannotation.class)!=null) {
            String invoke =(String) method.invoke(proxy, args);
            String s = invoke + "adadadadacacca";
            return s;
        }
        return method.invoke(proxy,args);
    }
}
