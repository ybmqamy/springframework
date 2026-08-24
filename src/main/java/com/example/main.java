package com.example;

import java.util.List;

public class main {
    public static void main(String[] args) {
        ResourceResolver rr = new ResourceResolver("");
        List<String> classList = rr.scan(res -> {
            String name = res.name(); // 资源名称"org/example/Hello.class"
            if (name.endsWith(".class")) { // 如果以.class结尾
                // 把"org/example/Hello.class"变为"org.example.Hello":
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            // 否则返回null表示不是有效的Class Name:
            return null;
        });
        System.out.println(classList);
    }
}
