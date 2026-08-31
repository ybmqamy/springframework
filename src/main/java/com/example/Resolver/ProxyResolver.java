package com.example.Resolver;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * 实现动态代理，基于java原生的的InvocationHandler拦截器
 * 由于原生的cglib已停止维护，这里采用最新的推荐bytebuddy
 */
/// aop的实现
/// 编译期：在编译时，由编译器把切面调用编译进字节码，这种方式需要定义新的关键字并扩展编译器，AspectJ就扩展了Java编译器，使用关键字aspect来实现织入；
/// 类加载器：在目标类被装载到JVM时，通过一个特殊的类加载器，对目标类的字节码重新“增强”；
/// 运行期：目标对象和切面都是普通Java类，通过JVM的动态代理功能或者第三方库实现运行期动态织入。
/// Spring实际上内置了多种代理机制，如果一个Bean声明的类型是接口，那么Spring直接使用Java标准库实现对接口的代理，如果一个Bean声明的类型是Class，那么Spring就使用CGLIB动态生成字节码实现代理。
/// cglib通过动态生成字节码来实现代理，用表达式实现aop匹配，容易漏掉或者匹配太大，这里采用基于annotation注解的形式来注入
///CGLIB（Code Generation Library）是一个高性能的代码生成库，主要用于为没有实现接口的类创建动态代理。它是对 JDK 动态代理的补充，尤其在需要代理普通类或追求更高性能时非常有用。
///CGLIB 通过动态生成目标类的子类，并在子类中拦截方法调用来实现代理逻辑。它使用 MethodInterceptor 接口来定义拦截逻辑，并通过 Enhancer 类生成代理对象。由于 CGLIB 是直接操作字节码，其性能优于基于反射的 JDK 动态代理。
///但很可惜cglib已经停止维护了，github推荐使用bytebuddy
/// 但同时我们也失去了对接口进行代理
/// 对于拦截器，我们选用java原生的inovcation，它定义了一个方法 invoke，这个方法会在代理实例上的方法被调用时执行。
/// 对于继承了InvocationHandler，需要重写其中的拦截器invoke方法
/// 这个本质上就是做了一个生成代理的功能，传入对应的拦截器和被代理对象后，会返回新代理对象的实例
public class ProxyResolver {
    ByteBuddy byteBuddy=new ByteBuddy();

    public <T> T createproxy(T bean, InvocationHandler handler){
        /// 整个过程大致对应如下，
        /// 1.获取需要被代理的bean 的字节码
        Class<?> aClass = bean.getClass();
        /// 2.ForLoadedType.of(superType),为aclass装载,选择默认的构造方法策略
        Class<?> proxyClass = this.byteBuddy.subclass(aClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR).
                /// 3.拦截所有的公共方法，创建新的拦截器对象，这里用原生的InvocationHandler
                        method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                                /// 一个新的拦截器
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
                                /// 这里没有定义任何的逻辑，直接返回原始的bean的方法
                                /// 如果定义了，调用定义在拦截器中的方法
                                return handler.invoke(bean, method,args);
                            }
                        }
                )).make().load(aClass.getClassLoader()).getLoaded();
        /// 4.生成对应的字节码并装载
        try {
           Object proxy=proxyClass.getConstructor().newInstance();
           return (T) proxy;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
