package com.nexus.backend.admin.common.result;

import lombok.Data;

/**
 * 统一返回结果
 *
 * @author yourcompany
 * @since 2024-01-01
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 错误状态码
     */
    public static final Integer ERROR_CODE = 500;

    public Result() {}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功返回
     *
     * @param message 消息
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 成功返回（无数据）
     *
     * @return 结果
     */
    public static Result<Void> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null);
    }

    /**
     * 错误返回
     *
     * @param message 错误消息
     * @return 结果
     */
    public static Result<Void> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    /**
     * 错误返回
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 结果
     */
    public static Result<Void> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

}
