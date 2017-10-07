package com.demos.spring.completedemo.util;

import java.io.*;

/**
 * 持久化工具
 *
 * @author xishang
 * @version 1.0
 * @date 2017/10/5
 */
public class SerializeUtils {

    public static <T extends Serializable> byte[] serialize(T t) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

}
