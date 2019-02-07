package com.demo.spring.starterdemo.mapper;

import com.demo.spring.starterdemo.domain.User;
import com.demo.spring.starterdemo.util.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where name = #{name}")
    User selectByName(@Param("name") String Name);

}
