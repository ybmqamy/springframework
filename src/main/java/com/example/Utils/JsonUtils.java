package com.example.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtils {
    /// 利用jakson处理json数据
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object readJson(BufferedReader reader, Class<?> classType) {
        /// bufferread是获取字节流，classtype是参数转换的的对象
        try {
            Object instance= objectMapper.readValue(reader, classType);
            return instance;
        } catch (IOException e) {
            throw new RuntimeException("字节转换异常");
        }
    }

    public static void writeJson(PrintWriter writer,Object data){
        try {
            objectMapper.writeValue(writer,data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
