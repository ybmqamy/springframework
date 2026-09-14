package com.example.Dispatcher;

import com.example.Annotation.Bean;

/// 封装返回的结果
public class Result {
    public final boolean success;
    public final Integer code;
    public final Object data;  ///响应得到的数据
    public Result(Boolean success, Integer code, Object data) {
        this.success = success;
        this.code = code;
        this.data = data;
    }


}
