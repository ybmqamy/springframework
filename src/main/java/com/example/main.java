package com.example;

import com.example.Bean.BeanDefinition;
import com.example.Jdbc.JdbcTemplate;
import com.example.Resolver.PropertyResolver;
import com.example.Resolver.ProxyResolver;
import com.example.Utils.YamlUtils;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.test.*;

import java.sql.SQLOutput;
import java.util.Map;
import java.util.Properties;

public class main {
    public static void main(String[] args) throws Exception {
        // 1. 读取 yaml 并拍平成 key1.key2 结构，值统一转成字符串（Properties 只认字符串）
        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
        Properties properties = new Properties();
        map.forEach((k, v) -> properties.put(k, String.valueOf(v)));
        PropertyResolver resolver = new PropertyResolver(properties);

        // 2. 启动容器（会触发扫描、实例化、注入、初始化）
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(AppConfig.class, resolver)) {

//            System.out.println("\n========== 容器启动完成，所有 bean ==========");
//            ctx.beans.values().forEach(def ->
//                    System.out.println("bean: " + def.getName() + " -> " + def.getInstance()));
//
//            System.out.println("\n========== 验证注入结果 ==========");
//            EmailService email = (EmailService) ctx.getBean("com.example.test.EmailService");
//            System.out.println("EmailService.title = " + email.getTitle());
//            System.out.println("EmailService.author = " + email.getAuthor());
//            System.out.println("EmailService.getMessage() = " + email.getMessage());
//
//            UserController controller = (UserController) ctx.getBean("com.example.test.UserController");
//            System.out.println("UserController.messageService = " + controller.getMessageService());
//            System.out.println("UserController.port = " + controller.getPort());
//            System.out.println("UserController.timeout = " + controller.getTimeout());
//            ///  primary生效了，同一个接口注入的是primary的
//            System.out.println("UserController.mapper = " + controller.getMapper())
            OriginBean bean = (OriginBean) ctx.getBean("OriginBean");
            /// SecondProxyBean@4590c9c3 此时拿到的就是被代理后的对象
            /// 此时second值更大，拿到更大的代理
            Object bean1 = ctx.getBean("FirstProxyBeanPostProcessor");
            System.out.println("================================");
            System.out.println(bean);
            System.out.println(bean1);

            System.out.println("=======================================");
            /// 这里bob没有注册成为bean

//            Bob createproxy = new ProxyResolver().createproxy(bob, new PoliteInvocationHandler());
//            /// 此时代理成功，输出的是加强的bob
//            System.out.println(createproxy.hello());
//            /// 此时生成的是代理后的对象
//            System.out.println(createproxy.getClass().getName());
            /// 有这个拦截器说明注册成为bean了
            /// FirstProxyBeanPostProcessor@68c72235 已经被注测了
            BeanDefinition politeInvocationHandler = ctx.getBeanDefinition("createAroundProxyBeanPostProcessor");
            System.out.println(politeInvocationHandler.getInstance());
            /// 拿到容器中的bob，发现是被代理加强过
            Bob bob = (Bob) ctx.getBean("Bob");
            /// 尝试事务，很成功
            System.out.println(bob.hello());
            /// Bob$ByteBuddy$Qj8ibLv8 是被代理后的对象
            String name = bob.getClass().getName();
            System.out.println(name);

        }
        // 3. try-with-resources 关闭，触发 @PreDestroy / destroyMethod
        System.out.println("\n========== 容器已关闭 ==========");
        /// 测试aop的效果,

    }
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

    /// spring提供了一个JdbcTemplate和NamedParameterJdbcTemplate模板类，可以方便地操作JDBC
    /// 我们需要实现的就是jdbctemplate
    /// jdbctemplate封装了对数据库的操作并支持声明式的事务

}
