package com.demo.spring.starterdemo.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/4
 */
@ConfigurationProperties(prefix = "login")
@Data
@Component
public class LoginProperties {

    private String userId;

    private String username;

    private String password;

}
