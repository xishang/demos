package com.demo.spring.starterdemo.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "username")
    private String username; // 用户名

//    @Column(name = "password")
    private String password; // 用户密码

//    @Column(name = "name")
    private String name; // 姓名

//    @Column(name = "phone")
    private String phone; // 手机号

//    @Column(name = "create_time")
    private String createTime; // 创建时间

//    @Column(name = "modified_time")
    private Date modifiedTime; // 更新时间

}
