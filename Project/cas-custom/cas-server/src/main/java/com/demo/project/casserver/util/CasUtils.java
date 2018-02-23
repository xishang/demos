package com.demo.project.casserver.util;

import com.demo.project.casserver.dto.TokenInfo;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public class CasUtils {

    // TGC -> 登出service集合
    private static final Map<String, Set<String>> TGC_SERVICE = new ConcurrentHashMap<>();

    private static final Map<String, String> tgcUser = new ConcurrentHashMap<>();

    // ticket -> username
    private static final Map<String, TokenInfo> tickets = new ConcurrentHashMap<>();

    // 用户认证成功, 添加一个新的TGC
    public static void addTgc(String tgc) {
        TGC_SERVICE.put(tgc, Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public static boolean tgcExist(String tgc) {
        return TGC_SERVICE.containsKey(tgc);
    }

    // 新应用认证, 添加service到TGC
    public static void addTgcService(String tgc, String service) {
        TGC_SERVICE.get(tgc).add(service);
    }

    public static void tgcLogout(String tgc) {
        for (String service : TGC_SERVICE.get(tgc)) {
            HttpUtils.post(service, "");
        }
        TGC_SERVICE.remove(tgc);
        tgcUser.remove(tgc);
    }

    public static void addTicket(String ticket, TokenInfo token) {
        tickets.put(ticket, token);
    }

    public static TokenInfo getTicket(String ticket) {
        return tickets.get(ticket);
    }

    public static void removeTicket(String ticket) {
        tickets.remove(ticket);
    }

    public static void setTgcUser(String tgc, String username) {
        tgcUser.put(tgc, username);
    }

    public static String getTgcUser(String tgc) {
        return tgcUser.get(tgc);
    }

}
