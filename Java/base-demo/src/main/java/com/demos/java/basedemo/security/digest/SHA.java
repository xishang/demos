package com.demos.java.basedemo.security.digest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;

import java.security.MessageDigest;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/2
 * <p>
 * SHA: Secure Hash Algorithm, 安全散列算法
 * <p>
 * [算法]-[摘要长度]-[实现方]:
 * -> [SHA-1]-[160]-[JDK]
 * -> [SHA-224]-[224]-[Bouncy Castle]
 * -> [SHA-256]-[256]-[JDK]
 * -> [SHA-384]-[384]-[JDK]
 * -> [SHA-512]-[512]-[JDK]
 */
public class SHA {

    public static String source = "消息摘要字符串";

    public static void main(String[] args) {
        jdkSHA1();
        bcSHA1();
        ccSHA1();
    }

    public static void jdkSHA1() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            byte[] result = messageDigest.digest(source.getBytes("utf-8"));
            System.out.println("JDK SHA1: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bcSHA1() {
        try {
            Digest digest = new SHA1Digest();
            digest.update(source.getBytes("utf-8"), 0, source.getBytes("utf-8").length);
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            System.out.println("BC SHA1: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ccSHA1() {
        try {
            System.out.println("CC SHA1: " + DigestUtils.sha1Hex(source.getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
