package com.demo.project.casserver.util;

import java.util.UUID;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/2/23
 */
public class IDGenerator {

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
