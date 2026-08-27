package com.example;

import com.example.Resolver.PropertyResolver;
import com.example.Utils.YamlUtils;
import com.example.context.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class main {
    public static void main(String[] args) throws NoSuchMethodException {
//        ResourceResolver rr = new ResourceResolver("");
//        List<String> classList = rr.scan(res -> {
//            String name = res.name(); // 资源名称"org/example/Hello.class"
//            if (name.endsWith(".class")) { // 如果以.class结尾
//                // 把"org/example/Hello.class"变为"org.example.Hello":
//                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
//            }
//            // 否则返回null表示不是有效的Class Name:
//            return null;
//        });
//        System.out.println(classList);
//        ///  读入整个配置文件
//        Properties properties=new Properties();
//        try(InputStream input=main.class.getClassLoader().getResourceAsStream("application.properties")){
//            properties.load(input);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        PropertyResolver propertyResolver=new PropertyResolver(properties);

//        Map<String, Object> map = YamlUtils.loadYamlAsFlatMap("application.yaml");
//        System.out.println(map);
//        Properties properties=new Properties();
//        properties.putAll(map);
//        System.out.println(properties.get("app.title"));


//        Properties properties=new Properties();
//        PropertyResolver propertyResolver=new PropertyResolver(properties);
//        propertyResolver.registerconvertor(Long.class,s -> Long.valueOf(s));
        /// 读取所有配置

//        Properties properties=new Properties();
//        Map<String, Object> map = YamlUtils.loadYamlAsPlainMap("application.yaml");
//        PropertyResolver propertyResolver=new PropertyResolver(properties);
        /// todo,现在扫描包可以扫到了，properties也可以获取到配置了，该注册bean了
        /// 完成bean的实例化，
        /// 在如果有构造方法，那么此时bean和构造方法是强依赖，必须同时完成创建与注入的工作
        /// 而如果是字段注入，就可以先创建实例，等创建后再注入，这两个过程可以分开
        /// 对于第一种循环依赖，我们暂时没有办法解决
        /// 但对于第二种循环依赖，我们只需要直接先创建再注入即可
        /// 原spring通过三级缓存机制解决循环依赖问题
    }

}
