package com.example.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/// 创建bean的主体，包括各种bean的信息
/// 因为bean是ioc自己创建的，所以我们需要根据注解，获取对应类的构造方法，来构造这个对象
public class BeanDefinition implements Comparable<BeanDefinition> {
    String name; ///名字

    Class<?> aClass; ///class对象
    /// 对于component定义的bean，没什么好说的，它对外声明的类型就是自己
    /// 但对于bean注解的对象，返回的可能是接口下面的子类，所以我这里直接返回的就是其声明类型，它对外声明是啥，我得到就是什么

    Object instance=null; ///bean的实例，刚开始没有构造，为空

    Constructor<?> constructor; ///获取bean的构造方法

    String FactoryName; ///bean工厂的名字
    Method FactoryMethod;

    int order;   ///bean的排序

    boolean primary;  ///是否表明为primary，当有多个相同的bean，给那个设置高优先级
    /// 初始化的方法名字和销毁的名字
    String initName;
    String destoryName;
    /// 初始化的方法和销毁的方法
    Method initMethod;
    Method destoryMethod;

    /// 用的还是这个构造方法，暂时不需要factory工厂
    public BeanDefinition(String name, Class<?> beanClass, Constructor<?> constructor, int order, boolean primary, String initMethodName,
                          String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.aClass = beanClass;
        this.constructor = constructor;
        this.FactoryName = null;
        this.FactoryMethod = null;
        this.order = order;
        this.primary = primary;
        constructor.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }

    public BeanDefinition() {
    }
    ///  重构比较器，按照正确的order字段排序
    @Override
    public int compareTo(BeanDefinition o) {
        return Integer.compare(this.order, o.order);
    }

    public BeanDefinition(String name, Class<?> aClass, Object instance, Constructor<?> constructor, String FactoryName, Method FactoryMethod, int order, boolean primary, String initName, String destoryName, Method initMethod, Method destoryMethod) {
        this.name = name;
        this.aClass = aClass;
        this.instance = instance;
        this.constructor = constructor;
        this.FactoryName = FactoryName;
        this.FactoryMethod = FactoryMethod;
        this.order = order;
        this.primary = primary;
        this.initName = initName;
        this.destoryName = destoryName;
        this.initMethod = initMethod;
        this.destoryMethod = destoryMethod;
    }

    private void setInitAndDestroyMethod(String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.initName = initMethodName;
        this.destoryName = destroyMethodName;
        if (initMethod != null) {
            initMethod.setAccessible(true);
        }
        if (destroyMethod != null) {
            destroyMethod.setAccessible(true);
        }
        this.initMethod = initMethod;
        this.destoryMethod = destroyMethod;
    }


    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return aClass
     */
    public Class<?> getAClass() {
        return aClass;
    }

    /**
     * 设置
     * @param aClass
     */
    public void setAClass(Class<?> aClass) {
        this.aClass = aClass;
    }

    /**
     * 获取
     * @return instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * 设置
     * @param instance
     */
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * 获取
     * @return constructor
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * 设置
     * @param constructor
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * 获取
     * @return FactoryName
     */
    public String getFactoryName() {
        return FactoryName;
    }

    /**
     * 设置
     * @param FactoryName
     */
    public void setFactoryName(String FactoryName) {
        this.FactoryName = FactoryName;
    }

    /**
     * 获取
     * @return FactoryMethod
     */
    public Method getFactoryMethod() {
        return FactoryMethod;
    }

    /**
     * 设置
     * @param FactoryMethod
     */
    public void setFactoryMethod(Method FactoryMethod) {
        this.FactoryMethod = FactoryMethod;
    }

    /**
     * 获取
     * @return order
     */
    public int getOrder() {
        return order;
    }

    /**
     * 设置
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 获取
     * @return primary
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * 设置
     * @param primary
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * 获取
     * @return initName
     */
    public String getInitName() {
        return initName;
    }

    /**
     * 设置
     * @param initName
     */
    public void setInitName(String initName) {
        this.initName = initName;
    }

    /**
     * 获取
     * @return destoryName
     */
    public String getDestoryName() {
        return destoryName;
    }

    /**
     * 设置
     * @param destoryName
     */
    public void setDestoryName(String destoryName) {
        this.destoryName = destoryName;
    }

    /**
     * 获取
     * @return initMethod
     */
    public Method getInitMethod() {
        return initMethod;
    }

    /**
     * 设置
     * @param initMethod
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * 获取
     * @return destoryMethod
     */
    public Method getDestoryMethod() {
        return destoryMethod;
    }

    /**
     * 设置
     * @param destoryMethod
     */
    public void setDestoryMethod(Method destoryMethod) {
        this.destoryMethod = destoryMethod;
    }

}
