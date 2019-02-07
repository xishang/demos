package com.demos.java.basedemo.security.encrypt.symmetric;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/6
 * <p>
 * DES: Data Encryption Standard, 数据加密标准
 * [实现方]-[密钥长度]-[工作模式]-[填充方式]:
 * [JDK]-[56]-[ECB, CBC, PCBC, CTR, CTS, CFB, CFB8~128, OFB, OFB8~128]-[NoPadding, PKCS5Padding, ISO10126Padding]
 * [BC]-[64]-[同上]-[PKCS7Padding, ISO10126d2Padding, X932Padding, ISO7816d4Padding, ZeroBytePadding]
 * <p>
 * 3DES: Triple Data Encryption Algorithm, 三重数据加密算法
 */
public class DES {

    public static String source = "对称加密字符串";

    public static void main(String[] args) {
        jdkDES();
        jdk3DES();
    }

    public static void jdkDES() {
        try {
            // 1.初始化密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
            keyGenerator.init(56);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] keyBytes = secretKey.getEncoded();

            // 2.密钥转换
            DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            Key convertKey = keyFactory.generateSecret(desKeySpec);

            // 3.加密
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, convertKey);
            byte[] encryptBytes = cipher.doFinal(source.getBytes("utf-8"));
            System.out.println("JDK DES encrypt: " + Hex.encodeHexString(encryptBytes));

            // 4.解密
            cipher.init(Cipher.DECRYPT_MODE, convertKey);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            System.out.println("JDK DES decrypt: " + new String(decryptBytes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jdk3DES() {
        try {
            // 1.初始化密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
            // SecureRandom会根据需要初始化对应长度
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] keyBytes = secretKey.getEncoded();

            // 2.密钥转换
            DESedeKeySpec desKeySpec = new DESedeKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            Key convertKey = keyFactory.generateSecret(desKeySpec);

            // 3.加密
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, convertKey);
            byte[] encryptBytes = cipher.doFinal(source.getBytes("utf-8"));
            System.out.println("JDK 3DES encrypt: " + Hex.encodeHexString(encryptBytes));

            // 4.解密
            cipher.init(Cipher.DECRYPT_MODE, convertKey);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            System.out.println("JDK 3DES decrypt: " + new String(decryptBytes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
