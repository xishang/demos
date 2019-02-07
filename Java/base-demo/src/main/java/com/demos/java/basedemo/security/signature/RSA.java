package com.demos.java.basedemo.security.signature;

import org.apache.commons.codec.binary.Hex;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/1
 */
public class RSA {

    private static String source = "数字签名字符串";

    public static void main(String[] args) {
        jdkRSA();
    }

    public static void jdkRSA() {
        try {
            // 1.初始化密钥
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

            // 2.执行签名
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(privateKey);
            signature.update(source.getBytes("utf-8"));
            byte[] result = signature.sign();
            System.out.println("JDK RSA 数字签名: " + Hex.encodeHexString(result));

            // 3.验证签名
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPublicKey.getEncoded());
            keyFactory = KeyFactory.getInstance("RSA"); // 为了演示，不需要重新赋值
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(publicKey);
            signature.update(source.getBytes("utf-8"));
            boolean flag = signature.verify(result);
            System.out.println("JDK RSA 验证结果: " + flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
