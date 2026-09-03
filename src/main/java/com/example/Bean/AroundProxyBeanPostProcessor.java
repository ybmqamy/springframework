package com.example.Bean;

import com.example.Annotation.Around;
import com.example.Exception.AopException;
import com.example.Resolver.ProxyResolver;
import com.example.Utils.ApplicationUtils;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.context.ApplicationContext;
import com.example.test.AppConfig;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/// 实现环绕的拦截器，本质上就是获取加了around注解的bean的实例,在调用对应方法之前，调用invoke执行对应拦截器的逻辑
/// 这个本身就是around，自带了自定义的功能
/// 假如我想自定义注解实现aop，那该怎么办，我们就可以吧注解写成一个泛型
/// 这样注解的定义就不再唯一，事务也可以很容易实现
public class AroundProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Around> implements BeanPostProcessor{
    /// 存放所有bean的实例
    Map<String,Object> beans=new HashMap<>();

    /// 核心实现
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        Class<?> aClass = bean.getClass();
        /// 只有你的实例上加了around注解，才能实现进行下一步操作
        Around around = aClass.getAnnotation(Around.class);
        if(around!=null) {
            String handlername;
            try {
                /// 拿到对应拦截器的名字
                handlername = (String) around.annotationType().getMethod("value").invoke(around);
            } catch (Exception e) {
                throw new AopException("aop异常");
            }///创建对应的代理对象
            Object proxy = createProxy(aClass, bean, handlername);
            /// 把原始的bean放回去
            beans.put(beanName, bean);
            return proxy;
        }else{
            return bean; ///如果为空，则说明不需要代理，直接返回原始对象即可
        }
    }
    /// 创建对应的代理对象
    Object createProxy(Class<?> clazz,Object bean,String handlername) throws Exception {
        AnnotationConfigApplicationContext applicationContext = (AnnotationConfigApplicationContext) ApplicationUtils.getRequiredApplicationContext();
        /// 由于原本的拦截器也被定义 为了一个bean，这里我们直接get即可
        /// 这里的拦截器的名字只是最简单的名字
        BeanDefinition bean1 = applicationContext.getBeanDefinition(handlername);
        /// 找到对应拦截器的实例
        /// 如果你这个拦截器为空，那我为你创建一个
        Object hander = bean1.getInstance();
        /// 无论bean有没有，只要我拦截器没有，我都为它创建一个，我跟bean就不绑定了
        /// 这里返回的是代理，我返回拦截器干什么
        if(hander==null){
            hander=applicationContext.createBeanAsEarlySingleton(bean1);
        }
        /// 如果拦截器不为空，则为其创建代理
        if(hander instanceof InvocationHandler handler){
            return new ProxyResolver().createproxy(bean, (InvocationHandler) hander);
        }else{
            throw new AopException("没有这个东西");
        }
    }

    /// 如果记录当前bean的代理实例不为空，则替换，否则返回原来的bean
    @Nullable
    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object o = this.beans.get(beanName);
        return o!=null? o: bean;
    }
}
