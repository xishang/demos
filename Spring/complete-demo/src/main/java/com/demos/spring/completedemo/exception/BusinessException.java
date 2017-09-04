package com.demos.spring.completedemo.exception;

public class BusinessException extends RuntimeException {

    private ErrorEnum errorEnum;

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.errorEnum = errorEnum;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    @Override
    public String toString() {
        return "[code:" + errorEnum.getCode() + ", message:" + errorEnum.getMessage() + "]";
    }

}
