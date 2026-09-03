package com.example.Invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BeforeInvocationHandlerAdapter implements InvocationHandler {
    public abstract Object before(Object proxy, Method method, Object[] args) throws InvocationTargetException;
    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        before(proxy,method,args);
        return method.invoke(proxy,args);
    }


}
