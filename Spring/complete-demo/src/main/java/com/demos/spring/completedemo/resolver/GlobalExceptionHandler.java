package com.demos.spring.completedemo.resolver;

import com.demos.spring.completedemo.bean.ResponseResult;
import com.demos.spring.completedemo.exception.BusinessException;
import com.demos.spring.completedemo.exception.ErrorEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

/**
 * Controller层统一异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseResult handleBusinessException(BusinessException e) {
        e.printStackTrace();
        return new ResponseResult(e.getErrorEnum());
    }

    /**
     * 处理SQL异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public ResponseResult handleSQLException(SQLException e) {
        e.printStackTrace();
        return new ResponseResult(ErrorEnum.SYSTEM_ERROR);
    }

    /**
     * 处理系统异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult handleSystemException(Exception e) {
        e.printStackTrace();
        return new ResponseResult(ErrorEnum.SYSTEM_ERROR);
    }

}
