package com.example;

import com.example.Resolver.PropertyResolver;
import com.example.Utils.YamlUtils;
import com.example.context.AnnotationConfigApplicationContext;
import com.example.test.*;

import java.util.Map;
import java.util.Properties;
import java.util.function.ObjDoubleConsumer;

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
//            System.out.println("UserController.mapper = " + controller.getMapper());
            OriginBean bean = (OriginBean) ctx.getBean("com.example.test.OriginBean");
            /// SecondProxyBean@4590c9c3 此时拿到的就是被代理后的对象
        /// 此时second值更大，拿到更大的代理
            Object bean1 = ctx.getBean("com.example.test.FirstProxyBeanPostProcessor");
            System.out.println("================================");
            System.out.println(bean);
            System.out.println(bean1);

        }
        // 3. try-with-resources 关闭，触发 @PreDestroy / destroyMethod
        System.out.println("\n========== 容器已关闭 ==========");
    }
}
