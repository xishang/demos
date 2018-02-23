package com.demo.project.casserver.dao;

import com.demo.project.casserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

}
