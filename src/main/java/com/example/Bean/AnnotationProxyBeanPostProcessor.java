package com.example.Bean;

import com.example.Annotation.Around;
import com.example.Exception.AopException;
import com.example.Resolver.ProxyResolver;
import com.example.Utils.ApplicationUtils;
import com.example.context.AnnotationConfigApplicationContext;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
/// 抽象出一个类， 定义一个泛型，可以自定义注解来实现aop
public abstract class AnnotationProxyBeanPostProcessor <A extends Annotation> implements BeanPostProcessor {
        Map<String, Object> beans = new HashMap<>();
        Class<A> annotationtype;

        public AnnotationProxyBeanPostProcessor() {
            this.annotationtype = getParameterizedType();
        }
        ///todo 明天完成这个地方
        private Class<A> getParameterizedType() {
            /// 获得当前类的直接父类对象，若对象带泛型，可以返回一个带泛型参数的ParameterizedType
            /// 为了实现指定注解的aop，我们必须确保拿到的对象一定是带泛型的ParameterizedType对象
            Type type = getClass().getGenericSuperclass();
            if(!(type instanceof ParameterizedType)){
                throw new AopException("参数异常");
            }
            ParameterizedType parameter = (ParameterizedType) type;
            /// 拿到指定的泛型类
            Type[] actualTypeArguments = parameter.getActualTypeArguments();
            /// 理论来讲，这个应该只有一个
            if(actualTypeArguments.length!=1){
                throw new AopException("传入的泛型参数异常"+parameter.getTypeName());
            }
            Type actualTypeArgument = actualTypeArguments[0];
            /// 因为要赋值给class，判断是否是一个具体的类
            if(!(actualTypeArgument instanceof Class<?>)){
                throw new AopException("没有这个类");
            }
            return (Class<A>) actualTypeArgument;
        }
        /// 只要是继承自它的都可以有这个方法
        @Nullable
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
            Class<?> aClass = bean.getClass();
            /// 只有你的实例上加了around注解，才能实现进行下一步操作
            /// 这里拿到对应的注解，注解上应该标注了拦截器的名字，就可以直接生成代理
            A around =aClass.getAnnotation(annotationtype);
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


