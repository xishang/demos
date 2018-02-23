package com.demo.project.casclient.util;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public class SessionUtils {

    private static final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    public static void setSession(HttpSession session) {
        sessionMap.put(session.getId(), session);
    }

    public static void invalidate(String sessionId) {
        sessionMap.get(sessionId).invalidate();
    }

}
