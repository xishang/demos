package com.demos.java.basedemo.security.encrypt.symmetric;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/7
 */
public class PBE {

    public static String source = "对称加密字符串";
    public static String password = "abc123456";

    public static void main(String[] args) {
        jdkPBE();
    }

    public static void jdkPBE() {
        try {
            // 1.初始化随机的8位字节的盐
            SecureRandom random = new SecureRandom();
            byte[] salt = random.generateSeed(8);

            // 2.使用口令初始化密钥
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
            SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // 3.加密
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
            Cipher cipher = Cipher.getInstance("PBEWITHMD5andDES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParameterSpec);
            byte[] encryptBytes = cipher.doFinal(source.getBytes("utf-8"));
            System.out.println("JDK PBE encrypt: " + Hex.encodeHexString(encryptBytes));

            // 4.解密
            cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParameterSpec);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            System.out.println("JDK PBE decrypt: " + new String(decryptBytes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
