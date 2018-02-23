package com.demo.project.casserver.service;

import com.demo.project.casserver.dao.UserRepository;
import com.demo.project.casserver.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/22
 */
@Service
public class UserService {

    @Resource
    private UserRepository userRepository;

    /**
     * 根据username查询用户
     *
     * @param username
     * @return
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 查询所有用户
     *
     * @return
     */
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /**
     * 查询用户
     *
     * @param id
     * @return
     */
    public User getById(Integer id) {
        return userRepository.getOne(id);
    }

    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    public User addUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    public User updateUser(User user) {
        User queryUser = userRepository.findOne(user.getId());
        if (queryUser == null) {
            return null;
        }
        return userRepository.save(user);
    }

    /**
     * 删除用户
     *
     * @param id
     */
    public void deleteUser(Integer id) {
        userRepository.delete(id);
    }

}
