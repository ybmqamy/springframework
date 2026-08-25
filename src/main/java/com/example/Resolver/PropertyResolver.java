package com.example.Resolver;

import com.example.Record.PropertyExpr;

import javax.annotation.Nullable;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

/// 提供配置查询的功能，便于后续配置的注入
///用 ClassLoader.getResourceAsStream(...) 去classpath 里找文件，所以文件必须落在 classpath 上才能被读到
/// 最后配置放到resource中的properties中
public class PropertyResolver {
    Map<String,String> properties=new HashMap<>();
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();
    /// java自带的配置解析功能
    public PropertyResolver(Properties prop){
        ///当前进程的所有环境变量，一次性复制到 properties 这个 Map 里
        this.properties.putAll(System.getenv());
        /// 把所有的环境变量，按照key-value的形式保存起来
        Set<String> property = prop.stringPropertyNames();
        for (String s : property) {
            this.properties.put(s,prop.getProperty(s));
        }
        /// 在构造函数中放入所有的转换器转换对象
        // String类型:
        converters.put(String.class, s -> s);
        // boolean类型:
        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));
        // int类型:
        converters.put(int.class, s -> Integer.parseInt(s));
        converters.put(Integer.class, s -> Integer.valueOf(s));
        // 其他基本类型...
        // Date/Time类型:
        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
        converters.put(Duration.class, s -> Duration.parse(s));
        converters.put(ZoneId.class, s -> ZoneId.of(s));
    }

    /// 获取对应的配置项，暂时只支持标准的格式
    @Nullable
    public String getProper(String key){
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr == null) {
            //            // 普通key查询: 直接查
            return this.properties.get(key);
        }
        String value = this.properties.get(keyExpr.key());
        // 带默认值: 查不到返回默认值；不带默认值: 查不到返回 null
        return value != null ?
                value : keyExpr.defaultValue();
    }
    /// 读取特定的类型，完成类型的转换
    public <T> T getProper(String key,Class<T> classtype){
        String value=getProper(key);
        if(value==null){
            return null;
        }
        return convert(classtype,value);
    }
    /// 类型的转换，本质就是从string转换到其他类型，返回的即为转换后的泛型
    private <T> T convert(Class<T> classtype, String value) {
        /// 根据对应的字节码拿到对应类型的映射函数
        Function<String, Object> stringObjectFunction = this.converters.get(classtype);
        if(stringObjectFunction==null){
            throw new IllegalArgumentException("您的字符非法");
        }
        return (T) stringObjectFunction.apply(value);
    }

    /// 解析具有默认值的配置，这里是依据字符串直接截取的
    PropertyExpr parsePropertyExpr(String key){
        if(key.startsWith("${")&&key.endsWith("}")){
            int n=key.indexOf(":");
            if(n==-1){
                /// 没有默认配置;
                String name = key.substring(2, key.length() - 1);
                return new PropertyExpr(name,null);
            }else{
                /// 有默认配置
                String name=key.substring(2,n);
                String value=key.substring(n+1,key.length()-1);
                return new PropertyExpr(name,value);
            }
        }
        return null;
    }

    /// 构造一个register，可以使得用户自定义转换对象
    public <T >void registerconvertor(Class<T> classtyp,Function<String,Object> function){
        this.converters.put(classtyp.getClass(),function);
    }

}
