package com.demos.java.basedemo.security.encrypt.symmetric;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/6
 * <p>
 * AES: Advanced Encryption Standard, 高级加密标准
 * [实现方]-[密钥长度]-[工作模式]-[填充方式]:
 * [JDK]-[128, 192, 256(256位密码需要获得无政策限制权限文件)]-[ECB, CBC, PCBC, CTR, CTS, CFB, CFB8~128, OFB, OFB8~128]-[NoPadding, PKCS5Padding, ISO10126Padding]
 * [BC]-[同上]-[同上]-[PKCS7Padding, ZeroBytePadding]
 */
public class AES {

    public static String source = "对称加密字符串";

    public static void main(String[] args) {
        jdkAES();
    }

    public static void jdkAES() {
        try {
            // 1.初始化密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            // SecureRandom会根据需要初始化对应长度
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] keyBytes = secretKey.getEncoded();

            // 2.密钥转换
            Key key = new SecretKeySpec(keyBytes, "AES");

            // 3.加密
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptBytes = cipher.doFinal(source.getBytes("utf-8"));
            System.out.println("JDK AES encrypt: " + Hex.encodeHexString(encryptBytes));

            // 4.解密
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            System.out.println("JDK AES decrypt: " + new String(decryptBytes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
