package com.thematrix.labmanagement.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一封装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable {

    private int code;
    private String msg;
    private Object data;

    public static Result success() {
        return new Result(200, "操作成功！", null);
    }

    public static Result success(String msg, Object data) {
        return new Result(200, msg, data);
    }

    public static Result success(String msg) {
        return new Result(200, msg, null);
    }

    public static Result success(Object data) {
        return new Result(200, "操作成功！", data);
    }

    public static Result fail(String data) {
        return new Result(400, "操作失败！", data);
    }

    public static Result error(String msg) {
        return new Result(-1, msg, null);
    }

    public static Result error(Integer code, String msg) {
        return new Result(code, msg, null);
    }
}
