package com.example.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/// 读取yaml的配置文件
public class YamlUtils {
    public static Map<String, Object> loadYamlAsPlainMap(String path) {
        /// 使用snakeyaml解析yaml的配置文件
        Yaml yaml=new Yaml();
        /// 获取当前类的类加载器
        try(InputStream input= YamlUtils.class.getClassLoader().getResourceAsStream(path)){
            ///其中嵌套结构也会被递归解析为 Map 或 List
            Map<String,Object> map=yaml.load(input);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// 读取yaml并拍平成 key1.key2.key3 的扁平结构，方便 putAll 进 Properties
    public static Map<String, Object> loadYamlAsFlatMap(String path) {
        Map<String, Object> map = loadYamlAsPlainMap(path);
        Map<String, Object> flat = new LinkedHashMap<>();
        flattenInto("", map, flat);
        return flat;
    }
    /// 递归拍平：嵌套 Map 用 . 连接 key，普通值直接落地
    /// 压制未检查类型转换的警告
    @SuppressWarnings("unchecked")
    private static void flattenInto(String prefix, Map<String, Object> source, Map<String, Object> target) {
        /// 遍历source，如果还是个map，再进入
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            /// 如果是个map，再递归
            if (value instanceof Map) {
                flattenInto(key, (Map<String, Object>) value, target);
            } else {
                target.put(key, value);
            }
        }
    }
}
