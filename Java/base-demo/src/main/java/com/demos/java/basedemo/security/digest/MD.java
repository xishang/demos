package com.demos.java.basedemo.security.digest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/2
 * MD系列-[128]
 */
public class MD {

    public static String source = "消息摘要字符串";

    public static void main(String[] args) {
        jdkMD5();
        jdkMD2();
        jdkBcMD4();
        bcMD4();
        ccMD5();
        ccMD2();
        saltMD5();
    }

    public static void jdkMD5() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] result = messageDigest.digest(source.getBytes("utf-8"));
            System.out.println("JDK MD5: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jdkMD2() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD2");
            byte[] result = messageDigest.digest(source.getBytes("utf-8"));
            System.out.println("JDK MD2: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jdkBcMD4() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            MessageDigest messageDigest = MessageDigest.getInstance("MD4");
            byte[] result = messageDigest.digest(source.getBytes("utf-8"));
            System.out.println("JDK MD4: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bcMD4() {
        try {
            Digest digest = new MD4Digest();
            digest.update(source.getBytes("utf-8"), 0, source.getBytes("utf-8").length);
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            System.out.println("BC MD4: " + Hex.encodeHexString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ccMD5() {
        try {
            System.out.println("CC MD5: " + DigestUtils.md5Hex(source.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void ccMD2() {
        try {
            System.out.println("CC MD2: " + DigestUtils.md2Hex(source.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void saltMD5() {
        try {
            byte[] salt = new byte[8];
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(salt); // 生成随机的盐

            /* JDK MD5 加盐 */
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(salt);
            // MessageDigest.digest(data) = update(data) + digest()
            byte[] jdkResult = messageDigest.digest(source.getBytes("utf-8"));
            System.out.println("JDK MD5 加盐: " + Hex.encodeHexString(jdkResult));

            /* BC MD5 加盐 */
            Digest digest = new MD5Digest();
            digest.update(salt, 0, salt.length);
            digest.update(source.getBytes("utf-8"), 0, source.getBytes("utf-8").length);
            byte[] bcResult = new byte[digest.getDigestSize()];
            digest.doFinal(bcResult, 0);
            System.out.println("BC MD5 加盐: " + Hex.encodeHexString(bcResult));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
