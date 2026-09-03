package com.example.Invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/// 重写before和after，只要有拦截器继承了这个，就必须重写对应的抽象方法after或before，然后唯一的区别是先调用invok执行方法，还是后调用invoke
public abstract class AfterInvocationHandlerAdapter implements InvocationHandler {
    public abstract Object after(Object proxy, Object returnValue, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException;
    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Object object = method.invoke(proxy, args);
        return after(proxy,object,method,args);
    }

}
