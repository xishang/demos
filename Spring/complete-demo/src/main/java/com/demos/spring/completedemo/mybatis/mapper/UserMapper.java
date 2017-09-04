package com.demos.spring.completedemo.mybatis.mapper;

import com.demos.spring.completedemo.bean.UserDO;
import com.demos.spring.completedemo.bean.UserQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    Integer insertUser(UserDO user);

    /**
     * 根据ID删除用户
     *
     * @param id
     * @return
     */
    Integer deleteUserById(@Param("id") Long id);

    /**
     * 获取用户列表
     *
     * @param query
     * @return
     */
    List<UserDO> listUser(UserQuery query);

    /**
     * 根据ID取出用户
     *
     * @param id
     * @return
     */
    UserDO getUserById(@Param("id") Long id);

    /**
     * 根据username取出用户
     *
     * @param username
     * @return
     */
    UserDO getUserByUsername(@Param("username") String username);

    /**
     * 根据手机号取出用户
     *
     * @param phone
     * @return
     */
    UserDO getUserByPhone(@Param("phone") String phone);

    /**
     * 根据username查询用户条数
     *
     * @param username
     * @return
     */
    Integer countUserByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户条数
     *
     * @param phone
     * @return
     */
    Integer countUserByPhone(@Param("phone") String phone);

    /**
     * 以ID为条件更新用户信息
     *
     * @param user
     * @return
     */
    Integer updateUser(UserDO user);

}
