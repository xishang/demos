package com.demo.spring.starterdemo.util;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 * <p>
 * 通用Mapper，该接口不能被扫描到，否则会出错
 * Mapper接口：基本的增、删、改、查方法
 * MySqlMapper接口：针对MySQL的额外补充接口，支持批量插入
 */
public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
