package com.example;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/// 提供配置查询的功能，便于后续配置的注入
///用 ClassLoader.getResourceAsStream(...) 去classpath 里找文件，所以文件必须落在 classpath 上才能被读到
/// 最后配置放到resource中的properties中
public class PropertyResolver {
    Map<String,String> properties=new HashMap<>();
    /// java自带的配置解析功能
    public PropertyResolver(Properties prop){
        ///当前进程的所有环境变量，一次性复制到 properties 这个 Map 里
        this.properties.putAll(System.getenv());
        /// 把所有的环境变量，按照key-value的形式保存起来
        Set<String> property = prop.stringPropertyNames();
        for (String s : property) {
            this.properties.put(s,prop.getProperty(s));
        }
    }
    /// 获取对应的配置项，暂时只支持标准的格式
    @Nullable
    public String getProper(String key){
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr == null) {
            // 普通key查询: 直接查
            return this.properties.get(key);
        }
        String value = this.properties.get(keyExpr.key());
        // 带默认值: 查不到返回默认值；不带默认值: 查不到返回 null
        return value != null ? value : keyExpr.defaultValue();
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

}
