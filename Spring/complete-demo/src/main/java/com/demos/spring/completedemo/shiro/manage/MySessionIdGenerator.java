package com.demos.spring.completedemo.shiro.manage;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * 自定义sessionId生成器
 */
public class MySessionIdGenerator implements SessionIdGenerator {

    @Override
    public Serializable generateId(Session session) {
        // 生成不带"-"的sessionId
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
