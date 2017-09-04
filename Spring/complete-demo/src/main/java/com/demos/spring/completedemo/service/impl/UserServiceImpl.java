package com.demos.spring.completedemo.service.impl;

import com.demos.spring.completedemo.bean.SimplePageInfo;
import com.demos.spring.completedemo.bean.UserDO;
import com.demos.spring.completedemo.bean.UserQuery;
import com.demos.spring.completedemo.exception.BusinessException;
import com.demos.spring.completedemo.exception.ErrorEnum;
import com.demos.spring.completedemo.mybatis.mapper.UserMapper;
import com.demos.spring.completedemo.service.UserService;
import com.demos.spring.completedemo.util.PasswordHelper;
import com.demos.spring.completedemo.util.SystemConstant;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void saveUser(UserDO user) {
        int countByUsername = userMapper.countUserByUsername(user.getUsername());
        if (countByUsername > 0) {
            // 该用户名已被注册
            throw new BusinessException(ErrorEnum.USERNAME_EXIST);
        }
        if (StringUtils.isNotEmpty(user.getPhone())) {
            int countByPhone = userMapper.countUserByPhone(user.getPhone());
            if (countByPhone > 0) {
                // 该手机号已绑定其他账户
                throw new BusinessException(ErrorEnum.PHONE_EXIST);
            }
        }
        // 密码加密存储
        user.setPassword(PasswordHelper.entryptPassword(user.getPassword().trim()));
        user.setStatus(SystemConstant.USER_STATUS_NORMAL);
        Date curTime = new Date();
        user.setCreateTime(curTime);
        user.setModifyTime(curTime);
        userMapper.insertUser(user);
    }

    @Override
    public int updateUser(UserDO user) {
        return userMapper.updateUser(user);
    }

    @Override
    public UserDO getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public UserDO getUserByPhone(String phone) {
        return userMapper.getUserByPhone(phone);
    }

    @Override
    public UserDO getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    @Override
    public SimplePageInfo<UserDO> listUser(UserQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<UserDO> userList = userMapper.listUser(query);
        return new SimplePageInfo<>(userList);
    }

}
