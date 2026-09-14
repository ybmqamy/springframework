package com.example.Utils;

import java.util.regex.Pattern;

public class PathUtils {
    public static Pattern compile(String urlPattern) {
        return Pattern.compile(urlPattern);
    }
    /// 解析路径参数
}
