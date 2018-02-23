package com.demo.project.casserver.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
@Data
@Entity
public class User {

    @Id
    @GeneratedValue
    private Integer id;

    private String username;

    private String password;

    private Integer age;

}
