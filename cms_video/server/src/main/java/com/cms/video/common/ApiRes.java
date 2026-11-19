package com.cms.video.common;

import lombok.Data;

@Data
public class ApiRes<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> ApiRes<T> ok(T data) {
        ApiRes<T> res = new ApiRes<>();
        res.code = 0;
        res.msg = "ok";
        res.data = data;
        return res;
    }

    public static <T> ApiRes<T> ok() {
        return ok(null);
    }

    public static <T> ApiRes<T> fail(String msg) {
        ApiRes<T> res = new ApiRes<>();
        res.code = 1;
        res.msg = msg;
        return res;
    }

    public static <T> ApiRes<T> fail(int code, String msg) {
        ApiRes<T> res = new ApiRes<>();
        res.code = code;
        res.msg = msg;
        return res;
    }
}
