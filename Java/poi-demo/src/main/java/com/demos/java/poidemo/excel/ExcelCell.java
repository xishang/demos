package com.demos.java.poidemo.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/7
 * excel列索引
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelCell {

    int cellIndex();

}
