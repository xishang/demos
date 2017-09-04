package com.demos.spring.completedemo.exception;

public enum ErrorEnum {

    SUCCESS("0000", "成功"),

    // 创建用户时异常:10xx
    USERNAME_EXIST("1001", "用户名已存在"),
    USERNAME_ILLEGAL("1002", "用户名格式不合法"),
    PASSWORD_ILLEGAL("1003", "密码格式不合法"),
    PHONE_EXIST("1004", "手机号已绑定"),

    // 登陆异常:20xx
    USERNAME_NOT_EXIST("2001", "用户名不存在"),
    PASSWORD_ERROR("2002", "密码错误"),
    VARIFY_CODE_ERROR("2003", "验证码错误"),
    ACCOUNT_LOCKED("2004", "账号被锁定"),

    // 权限验证异常

    // 系统异常:99xx
    SYSTEM_ERROR("9999", "系统异常");

    private String code;
    private String message;

    private ErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
