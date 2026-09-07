//package com.example;
//
//import com.example.Bean.BeanDefinition;
//import com.example.Jdbc.JdbcTemplate;
//import com.example.Resolver.PropertyResolver;
//import com.example.Resolver.ProxyResolver;
//import com.example.Utils.YamlUtils;
//import com.example.context.AnnotationConfigApplicationContext;
//import com.example.test.*;
//
//import java.sql.SQLOutput;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//public class main {
//    public static void main(String[] args) throws Exception {
//        // 1. 读取 yaml 并拍平成 key1.key2 结构，值统一转成字符串（Properties 只认字符串）
//        /// 创建annotation应该被抽象出来
//        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
//        Properties properties = new Properties();
//        map.forEach((k, v) -> properties.put(k, String.valueOf(v)));
//        PropertyResolver resolver = new PropertyResolver(properties);
//
//        // 2. 启动容器（会触发扫描、实例化、注入、初始化）
//        try (AnnotationConfigApplicationContext ctx =
//                     new AnnotationConfigApplicationContext(AppConfig.class, resolver)) {
//
////            System.out.println("\n========== 容器启动完成，所有 bean ==========");
////            ctx.beans.values().forEach(def ->
////                    System.out.println("bean: " + def.getName() + " -> " + def.getInstance()));
////
////            System.out.println("\n========== 验证注入结果 ==========");
////            EmailService email = (EmailService) ctx.getBean("com.example.test.EmailService");
////            System.out.println("EmailService.title = " + email.getTitle());
////            System.out.println("EmailService.author = " + email.getAuthor());
////            System.out.println("EmailService.getMessage() = " + email.getMessage());
////
////            UserController controller = (UserController) ctx.getBean("com.example.test.UserController");
////            System.out.println("UserController.messageService = " + controller.getMessageService());
////            System.out.println("UserController.port = " + controller.getPort());
////            System.out.println("UserController.timeout = " + controller.getTimeout());
////            ///  primary生效了，同一个接口注入的是primary的
////            System.out.println("UserController.mapper = " + controller.getMapper())
//            OriginBean bean = (OriginBean) ctx.getBean("OriginBean");
//            /// SecondProxyBean@4590c9c3 此时拿到的就是被代理后的对象
//            /// 此时second值更大，拿到更大的代理
//            Object bean1 = ctx.getBean("FirstProxyBeanPostProcessor");
//            System.out.println("================================");
//            System.out.println(bean);
//            System.out.println(bean1);
//
//            System.out.println("=======================================");
//            /// 这里bob没有注册成为bean
//
////            Bob createproxy = new ProxyResolver().createproxy(bob, new PoliteInvocationHandler());
////            /// 此时代理成功，输出的是加强的bob
////            System.out.println(createproxy.hello());
////            /// 此时生成的是代理后的对象
////            System.out.println(createproxy.getClass().getName());
//            /// 有这个拦截器说明注册成为bean了
//            /// FirstProxyBeanPostProcessor@68c72235 已经被注测了
////            BeanDefinition politeInvocationHandler = ctx.getBeanDefinition("createAroundProxyBeanPostProcessor");
////            System.out.println(politeInvocationHandler.getInstance());
////            /// 拿到容器中的bob，发现是被代理加强过
////            Bob bob = (Bob) ctx.getBean("Bob");
////            /// 尝试事务，很成功
////            System.out.println(bob.hello());
////            /// Bob$ByteBuddy$Qj8ibLv8 是被代理后的对象
////            String name = bob.getClass().getName();
////            System.out.println(name);
////            /// acount不该是一个bean，这是ioc的硬性约束,因为bean的参数只能来自容器
////            JdbcTemplate jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcTemplate");
////            List<account> users = jdbcTemplate.queryForList(
////                    "SELECT * FROM account",
////                    (rs, rowNum) -> new account(rs.getInt("id"),
////                            rs.getString("name"),
////                            rs.getInt("money"))
////            );
////            users.forEach(System.out::println);
////            jdbcTemplate.updata("update account set money= ? where id= ?",325,1);
////            jdbcTemplate.delete("delete from account where id=?",5);
//            EmailService emailService = (EmailService) ctx.getBean("EmailService");
//            emailService.insert("insert into account (id,name,money) values (?,?,?)", 1022, "ok", 100);   // 成功
//            emailService.insert("insert into account (id,name,money) values (?,?,?)", 1022, "dup", 200);     // 冲突，抛异常
//
//        }
//        // 3. try-with-resources 关闭，触发 @PreDestroy / destroyMethod
//        System.out.println("\n========== 容器已关闭 ==========");
//        /// 测试aop的效果,
//        /// 测试jdbc效果\
//
//    }
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

    /// springmvc，springmvc所包括的内容非常广泛，例如
    /// 一个DispatcherServlet作为核心处理组件，接收所有URL请求，然后按MVC规则转发；
    /// 基于@Controller注解的URL控制器，由应用程序提供，Spring负责解析规则；
    /// 提供ViewResolver，将应用程序的Controller处理后的结果进行渲染，给浏览器返回页面；
    /// 基于@RestController注解的REST处理机制，由应用程序提供，Spring负责将输入输出变为JSON格式；
    /// 多种拦截器和异常处理器等。
    /// 这里核心实现就是servlet，controller，view和restcontroller

    /// 服务器为一个应用程序提供一个“容器”，即Servlet Container，
    ///一个Server可以同时跑多个Container，不同的Container可以按URL、域名等区分，Container才是用来管理Servlet、Filter、Listener这些组件的：
    /// 而我们ioc本身也是一个容器，如何处理就很关键

    /// 对于servlet container各组件实例化的方法，也有很多
    /// 通过在web.xml配置文件中定义，这也是早期Servlet规范唯一的配置方式；
    /// 通过注解@WebServlet、@WebFilter和@WebListener定义，由Servlet容器自动扫描所有class后创建组件，这和我们用Annotation配置Bean，由IoC容器自动扫描创建Bean非常类似；
    /// 先配置一个Listener，由Servlet容器创建Listener，然后，Listener自己调用相关接口，手动创建Servlet和Filter。
    /// 这里我们选择的是第三种，对spring来讲，servlet和listener是固定的，而controller又是在ioc中被管理，所以第三种是最方便的
    /// 而第二种适合于组件都不固定，全部需要装配启动的时候
    /// 而webxml形式复杂，不太方便


    /// 所以我们的顺序，先闹一个servlet容器，然后创建一个listener，listener启动ioc，然后listener在容器中装配Spring内置的一个DispatcherServlet
    /// 分发servlet之后，前端的请求就可以找到ioc容器，再找到对应的controller，执行对应的方法

    /// 应用程序必须配置一个Summer Framework提供的Listener；
    /// Tomcat完成Servlet容器的创建后，立刻根据配置创建Listener；
    /// Listener初始化时创建IoC容器；
    /// Listener继续创建DispatcherServlet实例，并向Servlet容器注册；
    /// DispatcherServlet初始化时获取到IoC容器中的Controller实例，因此可以根据URL调用不同Controller实例的不同处理方法。

    /// 这就是原生的spring打包方式，先写完各种配置，什么datasource，最后打包成war，部署在tomcat上，即成功一个web应用的全程

//}
