package com.demos.spring.completedemo.shiro.manage;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

import java.io.Serializable;

/**
 * shiro的session操作
 * <p>
 * shiro通过SessionDAO的create()、update()、delete()等接口管理session缓存
 */
public class CacheSessionDAO extends CachingSessionDAO {

    /**
     * 生成sessionId
     */
    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        return sessionId;
    }

    /**
     * CachingSessionDAO已经重写了readSession()方法并且没有调用doReadSession()方法，此处可以返回null
     */
    @Override
    protected Session doReadSession(Serializable serializable) {
        return null;
    }

    @Override
    protected void doUpdate(Session session) {

    }

    @Override
    protected void doDelete(Session session) {

    }

}
