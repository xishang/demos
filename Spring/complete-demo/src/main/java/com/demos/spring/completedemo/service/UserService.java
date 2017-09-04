package com.demos.spring.completedemo.service;

import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.bean.UserDO;
import com.demos.spring.completedemo.bean.UserQuery;

public interface UserService {

    void saveUser(UserDO user);

    int updateUser(UserDO user);

    UserDO getUserByUsername(String username);

    UserDO getUserByPhone(String phone);

    UserDO getUserById(Long id);

    SimplePageInfo<UserDO> listUser(UserQuery query);

}
