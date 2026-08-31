package com.example.context;

import com.example.Annotation.*;
import com.example.Bean.BeanDefinition;
import com.example.Bean.BeanPostProcessor;
import com.example.Exception.*;
import com.example.Resolver.PropertyResolver;
import com.example.Resolver.ResourceResolver;
import com.example.Utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 创建bean的definition（分component和bean）
 * 完成bean的实例化（component和bean）
 */
public class AnnotationConfigApplicationContext implements AutoCloseable,ApplicationContext{

    public final Map<String, BeanDefinition> beans;

    public final PropertyResolver propertyResolver;

    public final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    /// 跟踪所有已创建bean的名字，如果bean尚未创建，则报错
    Set<String> createdbeansname;

    Logger logger= LoggerFactory.getLogger(getClass());

    /// 获取到所有bean的定义，现在已经可以找到bean了，我们需要扫描所有的包，找到class类，创建bean
    public AnnotationConfigApplicationContext(Class<?> config, PropertyResolver propertyResolver) throws NoSuchMethodException {
        this.propertyResolver=propertyResolver;
        Set<String> beanClassNames = scanForClassNames(config);
        this.beans=createbeans(beanClassNames);
        this.createdbeansname=new LinkedHashSet<>();///创建检测循环依赖
        /// 创建config定义的bean
        this.beans.values().stream().filter(this::isConfigurationDefinition).sorted().map(
                beanDefinition -> {
                    /// 需要单独为bean创建实例
                    try {
                        createBeanAsEarlySingleton(beanDefinition);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return beanDefinition.getName();
                }
        ).collect(Collectors.toList());
        /// 首先做类型的转换，对于所有返回了beanpost的bean，我们需要完成类型的转换
        /// 经过上面的实例化，所有的bean都应该已经有了一个实例,这里是完成实例的转换，转换为对应的beanpostprocessor的实例
        /// 如果它实现了beanpostprocessor实例，它一定能够转换成功的
        /// 这里直接转换即可
        List<BeanPostProcessor> processes = this.beans.values().stream().
                filter(this::isBeanPostProcessorDefinition).sorted().map(def ->{
                    Object beanAsEarlySingleton = null;
                    try {
                        beanAsEarlySingleton = createBeanAsEarlySingleton(def);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return (BeanPostProcessor)beanAsEarlySingleton;
                        }
                ).collect(Collectors.toList());
        this.beanPostProcessors.addAll(processes);
        /// 此时得到的就是记录着所有bean实例名字的list
        /// 再为其他无实例的bean创建
        /// 这里是为实例为空的bean创建实例，创建实例的过程也要修改一下
        List<BeanDefinition> beanDefinitions = this.beans.values().stream().
                filter(beanDefinition -> beanDefinition.getInstance() == null).collect(Collectors.toList());
        beanDefinitions.forEach(beanDefinition ->{
                    if(beanDefinition.getInstance()==null){
                        try {
                            createBeanAsEarlySingleton(beanDefinition);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
        });

        /// 现在创建弱依赖，对于弱依赖，我们可以先创建再注入，直接通过反射先创建
        this.beans.values().forEach(def -> {
            injectBean(def);
        });

        /// 调用init方法，完成弱依赖的初始化
        this.beans.values().forEach(def -> {
            initBean(def);
        });

    }
    /// 判断是否是beanpostprocessor：通过类判断（此时实例可能还没创建，不能靠 instance 判断）
    /// instanceof 是基于实例的，而assign它是不依赖于实例的
    private boolean isBeanPostProcessorDefinition(BeanDefinition beanDefinition) {
        return BeanPostProcessor.class.isAssignableFrom(beanDefinition.getAClass());
    }

    /// 完成全部的初始化
    private void initBean(BeanDefinition def) {
        callMethod(def.getInstance(), def.getInitMethod(), def.getInitName());
    }

    private void callMethod(Object instance, Method initMethod, String initName) {
        /// 完成初始化，如果一个方法没有初始化方法，只有名字，那么它就是bean方法，此时需要通过名字获取对应的初始化方法
        if(initMethod!=null){
            try {
                initMethod.invoke(instance);
            } catch (Exception e) {
                throw new BeanCreationException("bean创建失败");
            }
        }else if(initName!=null){
            Method method = ClassUtils.getnamemethod(instance.getClass(), initName);
            try {
                method.invoke(instance);
            } catch (Exception e) {
                throw new BeanCreationException("bean创建失败");
            }
        }
    }

    /// 在注入bean时，还有一个事情，就是若子类继承了父类，那么子类也可以获得父类的autowired
    private void injectBean(BeanDefinition def) {
        /// 先保留一下我bean的对象，如果没有指定，默认是原来的bean
        /// 这里一定拿到的是原始的bean，因为代理是加在类上的，原始的字段只能在原始的类中找到，代理没有
        Object beanInstance = getProxiedInstance(def);
        try {   /// 这里注入的就不再是def中封存的对象，而是得到的原始对象
            injectproperties(def,def.getAClass(),beanInstance);
        }catch (Exception e){
            throw new BeanCreationException("creating fail");
        }
    }
    /// 如果它期望得到的不是现在的类型，那么我就在实例化之后对比一下postset中指定的类型，如果不一样，那我再变回去
    /// 如果代理还需要字段注入，必须成对实现，如果不关心字段的注入，只依赖于代理的生成，那么是不需要实现这个的
    private Object getProxiedInstance(BeanDefinition def) {
        Object instance=def.getInstance();
        List<BeanPostProcessor> reversebeanprocessor=new ArrayList<>(this.beanPostProcessors);
        /// 对于配置了order 的代理，它根据字段生成代理，对字段更大的值覆盖更小的代理，这也就是为何倒排的原因
        Collections.reverse(reversebeanprocessor);
        for (BeanPostProcessor beanPostProcessor : reversebeanprocessor) {
            Object reserveinstance = beanPostProcessor.postProcessOnSetProperty(instance, def.getName());
            if(reserveinstance!=instance){
                instance=reserveinstance;
            }
        }
        return instance;
    }

    private void injectproperties(BeanDefinition def, Class<?> aClass, Object instance) throws Exception {
        /// 尝试注入，通过反射获取所有的字段和方法
        /// autowired的字段，autowired的方法
        for (Field f : aClass.getDeclaredFields()) {
            tryInjectProperties(def, aClass, instance, f);
        }
        for (Method m : aClass.getDeclaredMethods()) {
            tryInjectProperties(def, aClass, instance, m);
        }
        /// 在父类查找Field和Method并注入:
        Class<?> superClazz = aClass.getSuperclass();
        if (superClazz != null) {
            /// 虽然我的实例在我的父类那里，但我自己也是要的呀
            injectproperties(def, superClazz, instance);
        }
    }
    /// 这个地方先暂时这样，但只这样肯定不行，等我ioc完成全链路调试的再看这个
    private void tryInjectProperties(BeanDefinition def, Class<?> aClass, Object instance, AccessibleObject acc) throws Exception {
        /// 尝试为字段注入
        Value value = acc.getAnnotation(Value.class);
        Autowired autowired = acc.getAnnotation(Autowired.class);
        if (value == null && autowired == null) {
            return;
        }
        /// 这里是负责把bean的实例注入到对应的字段中，在createbean实例，传入的是bean的一些构造参数，这里需要把具体的bean注入到指定的字段中
        Field field = null;
        Method method = null;
        /// field字段的注入
        if (acc instanceof Field f) {
            checkFieldOrMethod(f);
            f.setAccessible(true);
            field = f;
        }
        /// 方法字段赋值
        /// 这个传入的参数必须是唯一的那个bean
        if (acc instanceof Method m) {
            checkFieldOrMethod(m);
            if (m.getParameters().length != 1) {
                throw new BeanDefinitionException(
                        String.format("Cannot inject a non-setter method %s for bean '%s': %s", m.getName(), def.getName(), def.getAClass().getName()));
            }
            m.setAccessible(true);
            method = m;
        }

        String accessibleName = field != null ? field.getName() : method.getName();
        Class<?> accessibleType = field != null ? field.getType() : method.getParameterTypes()[0];

        if (value != null && autowired != null) {
            throw new BeanCreationException(String.format("Cannot specify both @Autowired and @Value when inject %s.%s for bean '%s': %s",
                    aClass.getSimpleName(), accessibleName, def.getName(), def.getAClass().getName()));
        }

        /// @Value注入:注入bean的value所做的配置
        /// set和invoke都是同理，第一个参数是往哪里注入，第二个参数是注入谁
        if (value != null) {
            Object propValue = this.propertyResolver.getProper(value.value(), accessibleType);
            if (field != null) {
                logger.atDebug().log("Field injection: {}.{} = {}", def.getAClass().getName(), accessibleName, propValue);
                field.set(instance, propValue);
            }
            if (method != null) {
                logger.atDebug().log("Method injection: {}.{} ({})", def.getAClass().getName(), accessibleName, propValue);
                method.invoke(instance, propValue);
            }
        }

        // @Autowired注入:
        if (autowired != null) {
            String name = autowired.name();
            boolean required = autowired.value();
            /// 这里先通过bean的class去找
            BeanDefinition bean = (BeanDefinition) findBean(accessibleType);
            if (required && bean == null) {
                throw new UnsatisfiedDependencyException(String.format("Dependency bean not found when inject %s.%s for bean '%s': %s",aClass.getSimpleName(),
                        accessibleName, def.getName(), def.getAClass().getName()));
            }
            if (bean != null) {
                if (field != null) {
                    logger.atDebug().log("Field injection: {}.{} = {}", def.getAClass().getName(), accessibleName, bean);
                    field.set(instance, bean.getInstance());
                }
                if (method != null) {
                    logger.atDebug().log("Mield injection: {}.{} ({})", def.getAClass().getName(), accessibleName, bean);
                    method.invoke(instance, bean.getInstance());
                }
            }
        }
    }
    /// 检查一下这个参数是不是静态的，是不是final了
    private void checkFieldOrMethod(Member member) {
        int modifiers = member.getModifiers();
        if(Modifier.isFinal(modifiers)){
            throw new BeanCreationException("不可以传入静态的");
        }else if(Modifier.isStatic(modifiers)){
            throw new BeanCreationException("不可以传入静态的方法");
        }
    }

    /// 创建bean的单实例对象
    public Object createBeanAsEarlySingleton(BeanDefinition beanDefinition) throws Exception {
        /// 如果a依赖了b，b依赖了a，那么当转一圈回来时，一定会有重复的实例
        if(!createdbeansname.add(beanDefinition.getName())){
            throw new UnsatisfiedDependencyException("circulate dependency");
        }
        /// 这里目前是通过构造方法获得指定的构造器对象
        /// 这里已经判断了是靠构造方法还是靠工厂实例化
        Executable executable =
                beanDefinition.getFactoryName() == null ? beanDefinition.getConstructor() : beanDefinition.getFactoryMethod();
        /// 我这里要为构造方法填入参数
        final Parameter[] parameters = executable.getParameters();
        final Annotation[][] parametersAnnos = executable.getParameterAnnotations();
        Object[] args = new Object[parameters.length];
        /// todo强依赖的注入，如果参数中有value和autowired，那么必须创建和注入一起完成，且遇到循环依赖就爆炸
        for (int i = 0; i < parameters.length; i++) {
            final Parameter param = parameters[i];
            final Annotation[] paramAnnos = parametersAnnos[i];
            final Value value = ClassUtils.findAnnotationParameter(paramAnnos, Value.class);
            final Autowired autowired = ClassUtils.findAnnotationParameter(paramAnnos, Autowired.class);

            /// @Configuration类型的Bean是工厂，不允许使用@Autowired创建:
            final boolean isConfiguration = isConfigurationDefinition(beanDefinition);
            if (isConfiguration && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when create @Configuration bean '%s': %s.", beanDefinition.getName(), beanDefinition.getAClass().getName()));
            }

            // 参数需要@Value或@Autowired两者之一:
            if (value != null && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify both @Autowired and @Value when create bean '%s': %s.", beanDefinition.getName(), beanDefinition.getAClass().getName()));
            }
            if (value == null && autowired == null) {
                throw new BeanCreationException(
                        String.format("Must specify @Autowired or @Value when create bean '%s': %s.", beanDefinition.getName(), beanDefinition.getAClass().getName()));
            }
            /// 拿到参数类型:
            final Class<?> type = param.getType();
            if (value != null) {
                /// 参数是value，我调取配置写入参数
                args[i] = this.propertyResolver.getProper(value.value(), type);
            } else {
                /// 参数是@Autowired,我要找到对应的bean对象，递归创建所有依赖的对象
                String name = autowired.name();
                boolean required = autowired.value();
                /// 获取依赖的BeanDefinition:  这里就判断了是不是paimary 的字段
                BeanDefinition dependsOnDef = (BeanDefinition) findBean(type);
                /// 检测required==true? 如果不等于true，说明bean缺失，我要创建别的bean
                if (required && dependsOnDef == null) {
                    throw new BeanCreationException(String.format("Missing autowired bean with type '%s' when create bean '%s': %s.", type.getName(),
                            beanDefinition.getName(), beanDefinition.getAClass().getName()));
                }
                if (dependsOnDef != null) {
                    // 获取依赖Bean:
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    if (autowiredBeanInstance == null && !isConfiguration) {
                        /// 当前依赖Bean尚未初始化，递归调用初始化该依赖Bean:
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                } else {
                    args[i] = null;
                }
            }
        }
        /// arg装载的是全部需要的arg参数,接下来创建实例就好了
        Object instance=null;
        if(beanDefinition.getFactoryName()==null){
            try {
                instance=beanDefinition.getConstructor().newInstance(args);
            }catch (Exception e){
                throw new BeanCreationException("创建异常");
            }
        }else{
            /// 如果是bean的对象创建，那么我需要通过factory来获得对应的对应的实例
            /// factory工厂的方法是它本身，我在beandefinition时候已经定了，把bean的factory单独拿出来了，我只需要通过getbean，拿到它的实例
            ///  就是它自己，它自己就是个方法，直接调用即可
            Object configInstance = getBean(beanDefinition.getFactoryName());
            try {
                instance = beanDefinition.getFactoryMethod().invoke(configInstance, args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", beanDefinition.getName(), beanDefinition.getAClass().getName()));
            }
        }
        /// 把创建好的实例存回BeanDefinition，否则getInstance()永远是null
        beanDefinition.setInstance(instance);
        /// 为每个实例执行beanpostprocessor，执行里面定义的过程，这里已经全部替换了，但有一些是不需要设置新的bean的，而是设置原始的bean，所以我们需要让beanpostprocessor保存对应的实例
        /// 这里调换了创建的逻辑，先去完善beanpostprocessor，然后再创建普通的实例，当创建普通实例时postprocessor已经填充好了，这时直接替换替换即可
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            Object beanpostprocessor = beanPostProcessor.postProcessBeforeInitialization(beanDefinition.getInstance(), beanDefinition.getName());
            /// 如果当前实例和原本定义的bean实例不一样，肯定要替换一下
            /// 这里拿设置的实例更新，不然会出错
            if(beanDefinition.getInstance()!=beanpostprocessor){
                beanDefinition.setInstance(beanpostprocessor);
            }
        }
        return beanDefinition.getInstance();
    }

    /// 通过名字找到bean
    public <T> T findBean(Class<T> name){
        /// 这里找到属于同一个接口下的所有bean
        List<BeanDefinition> beans = (List<BeanDefinition>) findBeans(name);
        if(beans.isEmpty()){
            return null;
        }
        /// 如果只有唯一一个def，直接返回即可
        if(beans.size()==1){
            return (T) beans.get(0);
        }
        /// 如果有多个实例，我需要找谁标注了primary，通过primary标注优先返回哪一个
        List<BeanDefinition> primarybeanDefinitions = beans.stream().filter(beanDefinition ->
                beanDefinition.isPrimary()).collect(Collectors.toList());
        if(primarybeanDefinitions.size()==1){
            return (T) primarybeanDefinitions.get(0);
        }
        if(primarybeanDefinitions.isEmpty()){
            try {
                throw new NoUniqueBeanDefinitionException("No sole bean，AprilFrameWork not find @Primary specified");
            } catch (NoUniqueBeanDefinitionException e) {
                throw new RuntimeException(e);
            }
        }else if(primarybeanDefinitions.size()>1){
            try {
                throw new NoUniqueBeanDefinitionException("multitude of beans, @Primary should be specified");
            } catch (NoUniqueBeanDefinitionException e) {
                throw new RuntimeException(e);
            }
        }
        /// 所有的情况都已经判断完成，直接返回即可
        return null;
    }
    /// 对于返回类型不一致的bean，比如若干个bean返回都是某一个类的子类，我们必须遍历每一个bean，找到符合的bean
    public <T> List<T> findBeans(Class<T> requiredType){
        /// isAssignableFrom（或接口）是否可以被赋值给另一个类（或接口）。简单来说，它用来检查类型之间的兼容性
        /// 如果是实现的接口，那么可以赋值，说明我找到了属于同一个接口下的所有bean
        ///  类似于instanceof 但是它不依赖于具体的实例
        return (List<T>) this.beans.values().stream().filter(
                    new Predicate<BeanDefinition>() {
                        @Override
                        public boolean test(BeanDefinition beanDefinition) {
                            return requiredType.isAssignableFrom(beanDefinition.getAClass());
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

    private void scanFactoryMethod(String beanName, Class<?> clazz, Map<String, BeanDefinition> definitionMap) {
        /// 为bean注解创建bean
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            /// 是否带有bean的注解
            Bean bean = declaredMethod.getAnnotation(Bean.class);
            if (bean == null) {
                continue;
            }
            /// 根据我们的记忆，bean的返回值就是声明对象的类型
            Class<?> returnType = declaredMethod.getReturnType();
            ///根据method创建对应的bean,因为bean是跟着这个方法走的
            BeanDefinition rr = new BeanDefinition(
                    ClassUtils.getBeanName(declaredMethod),  // name
                    returnType,                               // aClass（声明类型）
                    null,                                     // instance
                    null,                                     // constructor（工厂Bean不需要构造器）
                    beanName,                                 // FactoryName = 配置类bean名
                    declaredMethod,                           // FactoryMethod = @Bean方法本身
                    getOrder(declaredMethod),                 // order
                    declaredMethod.isAnnotationPresent(Primary.class), // primary
                    bean.initMethod().isEmpty() ? null : bean.initMethod(),    // initName
                    bean.destroyMethod().isEmpty() ? null : bean.destroyMethod(), // destroyName
                    null, null                                // initMethod / destroyMethod
            );
            declaredMethod.setAccessible(true);
            addBeanDefinition(definitionMap, rr);
        }
    }

    /// 技术力有限，目前一个bean只能一个名字
    private void addBeanDefinition(Map<String, BeanDefinition> definitionMap, BeanDefinition def) {
        if (definitionMap.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    private Set<String> scanForClassNames(Class<?> config) {
        /// 获取加了注解的扫描包,把得到需要扫描的范围
        ComponentScan scan = ClassUtils.findAnnotation(config, ComponentScan.class);
        /// 有配置扫描包就直接得到扫描包，没有配置就选择config指定的包
        String[] scanpackage;
        if(scan.value()==null||scan.value().length==0){
            scanpackage=new String[] {config.getName()};
        }else{
            scanpackage=scan.value();
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
    /// 判断是否加入config注解
    private boolean isConfigurationDefinition(BeanDefinition beanDefinition){
        Configuration annotation = beanDefinition.getAClass().getAnnotation(Configuration.class);
        return annotation != null;
    }
    /// 通过名字拿到bean实例，未创建则先创建（配置类作为工厂时用它拿到工厂实例）
    public Object getBean(String name) {
        BeanDefinition def = this.beans.get(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with name '%s'.", name));
        }
        Object instance = def.getInstance();
        if (instance == null) {
            try {
                instance = createBeanAsEarlySingleton(def);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
    ///  在applicationcontext关闭时自动销毁所有的bean的实例
    @Override
    public void close(){
        beans.values().stream().forEach(beanDefinition -> {
            destoryBean(beanDefinition.getInstance(),beanDefinition.getDestoryMethod(),beanDefinition.getDestoryName());
        });
    }

    private void destoryBean(Object beanDefinition, Method destoryMethod, String destoryName) {
        if(destoryMethod!=null){
            try {
                destoryMethod.invoke(beanDefinition);
            }catch (Exception e){
                throw new DestroyBeanFailedException("销毁bena失败");
            }
        }else if(destoryName!=null){
            Method method = ClassUtils.getnamemethod(beanDefinition.getClass(), destoryName);
            try {
                method.invoke(beanDefinition);
            }catch (Exception e){
                throw new DestroyBeanFailedException("销毁bean失败");
            }
        }
    }

    @Override
    public boolean containsBean(String name) {
        for (BeanDefinition def : this.beans.values()) {
            if(def.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T findBean(String name) {
        T t = (T) this.beans.get(name);
        if(t==null){
            throw new NoSuchBeanDefinitionException("没有这个东西");
        }
        return t;
    }
}
