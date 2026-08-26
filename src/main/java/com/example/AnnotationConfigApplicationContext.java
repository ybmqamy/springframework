package com.example;

import com.example.Annotation.*;
import com.example.Bean.BeanDefinition;
import com.example.Exception.BeanDefinitionException;
import com.example.Exception.NoUniqueBeanDefinitionException;
import com.example.Resolver.PropertyResolver;
import com.example.Resolver.ResourceResolver;
import com.example.Utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.EventRecordingLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

///获得到bean的
public class AnnotationConfigApplicationContext {
    Map<String, BeanDefinition> beans;
    Logger logger= LoggerFactory.getLogger(getClass());
    /// 获取到所有bean的定义，现在已经可以找到bean了，我们需要扫描所有的包，找到class类，创建bean
    public AnnotationConfigApplicationContext(Class<?> config, PropertyResolver propertyResolver) throws NoSuchMethodException {
        Set<String> beanClassNames = scanForClassNames(config,propertyResolver);
        this.beans=createbeans(beanClassNames);
    }
    /// 通过名字找到bean
    public BeanDefinition findBean(Class<?> name) throws Exception {
        /// 这里找到属于同一个接口下的所有bean
        List<BeanDefinition> beans = findBeans(name);
        if(beans.isEmpty()){
            return null;
        }
        /// 如果只有唯一一个实例，直接返回即可
        if(beans.size()==1){
            return beans.get(0);
        }
        /// 如果有多个实例，我需要找谁标注了primary，通过primary标注优先返回哪一个
        List<BeanDefinition> primarybeanDefinitions = beans.stream().filter(beanDefinition ->
                beanDefinition.isPrimary()).collect(Collectors.toList());
        if(primarybeanDefinitions.size()==1){
            return primarybeanDefinitions.get(0);
        }
        if(primarybeanDefinitions.isEmpty()){
            throw new NoUniqueBeanDefinitionException("No sole bean，AprilFrameWork not find @Primary specified");
        }else if(primarybeanDefinitions.size()>1){
            throw new NoUniqueBeanDefinitionException("multitude of beans, @Primary should be specified");
        }
        /// 所有的情况都已经判断完成，直接返回即可
        return null;
    }
    /// 对于返回类型不一致的bean，比如若干个bean返回都是某一个类的子类，我们必须遍历每一个bean，找到符合的bean
    public List<BeanDefinition> findBeans(Class<?> type){
        /// isAssignableFrom（或接口）是否可以被赋值给另一个类（或接口）。简单来说，它用来检查类型之间的兼容性
        /// 如果是实现的接口，那么可以赋值，说明我找到了属于同一个接口下的所有bean
        ///  类似于instanceof 但是它不依赖于具体的实例
        return this.beans.values().stream().filter(
                new Predicate<BeanDefinition>() {
                    @Override
                    public boolean test(BeanDefinition beanDefinition) {
                        return type.isAssignableFrom(beanDefinition.getClass());
                    }
                }
        ).sorted().collect(Collectors.toList());
    }

    private Map<String, BeanDefinition> createbeans(Set<String> beanClassNames) throws NoSuchMethodException {
        Map<String,BeanDefinition> definitionMap=new HashMap<>();
        /// 找到所有的class对应的bean，把每一个BeanDefinition构造出来
        for (String beanClassName : beanClassNames) {
            /// 根据之前扫描到的bean的class，通过class自带的方法找到对应的bean
            Class<?> clazz=null;
            try {
                clazz=Class.forName(beanClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            /// 找到了对应的class，就可以通过反射获得到所有beandefinition的一切
            /// 首先看看这个扫描包下的class有没有对应的注释，如果有，说明它应该被定义成bean
            /// 这里先把它们分开处理
            Component component = ClassUtils.findAnnotation(clazz, Component.class);
            Service service = ClassUtils.findAnnotation(clazz, Service.class);
            Mapper mapper = ClassUtils.findAnnotation(clazz, Mapper.class);
            Controller controller = ClassUtils.findAnnotation(clazz, Controller.class);
            if(controller!=null||service!=null||mapper!=null||component!=null){
                String beanName = ClassUtils.getBeanName(clazz);
                BeanDefinition def = new BeanDefinition(
                        beanName, clazz, getSuitableConstructor(clazz),
                        getOrder(clazz), clazz.isAnnotationPresent(Primary.class),
                        // init/destroy方法名称:
                        null, null,
                        // 查找@PostConstruct方法:
                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                        // 查找@PreDestroy方法:
                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                /// 这里传递影响到同一个原对象
                addBeanDefinition(definitionMap,def);
                /// 检查是否有configuration的注解
                Configuration annotation = ClassUtils.findAnnotation(clazz, Configuration.class);
                if(annotation!=null){
                    /// 如果是有config注解的类， 那么里面可能有bean方法
                    scanFactoryMethod(beanName,clazz,definitionMap);
                }
            }
        }
        return definitionMap;
    }

    private void scanFactoryMethod(String beanName, Class<?> clazz, Map<String, BeanDefinition> definitionMap) throws NoSuchMethodException {
        /// 为bean注解创建bean
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            /// 是否带有bean的注解
            Bean bean = declaredMethod.getAnnotation(Bean.class);
            /// 根据我们的记忆，bean的返回值就是声明对象的类型
            Class<?> returnType = declaredMethod.getReturnType();
            ///根据method创建对应的bean,因为bean是跟着这个方法走的
            var rr=new BeanDefinition(
                    ClassUtils.getBeanName(declaredMethod),
                    returnType,
                    getSuitableConstructor(returnType),
                    getOrder(declaredMethod),
                    declaredMethod.isAnnotationPresent(Primary.class),
                    /// 这是调用bean自己的初始化方法和销毁方法，此时不可以用和类一样的方法来构造了，因为它自己也是个方法
                    bean.initMethod().isEmpty() ? null : bean.initMethod(),
                    // destroy方法名称:
                    bean.destroyMethod().isEmpty() ? null : bean.destroyMethod(),
                    // @PostConstruct / @PreDestroy方法:
                    null, null
            );
            addBeanDefinition(definitionMap,rr);
        }
    }

    /// 技术力有限，目前一个bean只能一个名字
    private void addBeanDefinition(Map<String, BeanDefinition> definitionMap, BeanDefinition def) {
        if (definitionMap.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    private Set<String> scanForClassNames(Class<?> config,PropertyResolver propertyResolver) {
        /// 获取加了注解的扫描包,把得到需要扫描的范围
        ComponentScan scan = ClassUtils.findAnnotation(config, ComponentScan.class);
        /// 有配置扫描包就直接得到扫描包，没有配置就选择config指定的包
        String[] scanpackage;
        if(scan.value()==null||scan.value().length==0){
            scanpackage=new String[] {config.getClass().getName()};
        }else{
            scanpackage=new String[] {Arrays.toString(scan.value())};
        }
        Set<String> classname=new LinkedHashSet<>();
        /// 扫描到这些包下的class,把每个beanclass的名字装入
        for (String packages : scanpackage) {
            logger.info("正在扫描包"+packages);
            ResourceResolver rr=new ResourceResolver(packages);
            List<String> res = rr.scan(resource -> {
                String name = resource.name();
                if (name.endsWith(".class")) {
                    return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
                }
                return null;
            });
            classname.addAll(res);
        }
        /// 再检查有无import形式的导入bean
        Import annotation = config.getAnnotation(Import.class);
        if(annotation!=null){
            for (Class<?> importConfigClass : annotation.value()) {
                String importClassName = importConfigClass.getName();
                classname.add(importClassName);
            }
        }
        return classname;
    }
    /// 获取对应的加了order注释的方法
    public int getOrder(Class<?> aclazz){
        Order order = aclazz.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }
    public int getOrder(Method method){
        Order order = method.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }
    /// 获取合适的构造方法
    public Constructor<?> getSuitableConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] cons = clazz.getConstructors();
        if (cons.length == 0) {
            cons = clazz.getDeclaredConstructors();
            if (cons.length != 1) {
                throw new BeanDefinitionException("More than one constructor found in class " + clazz.getName() + ".");
            }
        }
        if (cons.length != 1) {
            throw new BeanDefinitionException("More than one public constructor found in class " + clazz.getName() + ".");
        }
        return cons[0];
    }


}
