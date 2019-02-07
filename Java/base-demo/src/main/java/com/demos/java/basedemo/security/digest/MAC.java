package com.demos.java.basedemo.security.digest;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/5
 * <p>
 * MAC: Message Authentication Code, 消息认证码算法(含有密钥的散列函数算法)
 * 也称为: HMAC(keyed-Hash Message Authentication Code)
 * 融合MD和SHA算法
 * [算法]-[摘要长度]-[实现方]:
 * [HmacMD2]-[128]-[Bouncy Castle]
 * [HmacMD4]-[128]-[Bouncy Castle]
 * [HmacMD5]-[128]-[JDK]
 * [HmacSHA1]-[160]-[JDK]
 * [HmacSHA224]-[224]-[Bouncy Castle]
 * [HmacSHA256]-[256]-[JDK]
 * [HmacSHA384]-[384]-[JDK]
 * [HmacSHA512]-[512]-[JDK]
 */
public class MAC {

    public static String source = "消息摘要字符串";

    public static void main(String[] args) {
        hmacMD5();
    }

    public static void hmacMD5() {
        try {
            // 初始化KeyGenerator
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");
            // 产生密钥
            SecretKey secretKey = keyGenerator.generateKey();
            // 获取密钥字节数组
            byte[] keyBytes = secretKey.getEncoded();

            /* JDK Hmac */
            // 还原密钥
            SecretKey restoreSecretKey = new SecretKeySpec(keyBytes, "HmacMD5");
            // 实例化Mac
            Mac mac = Mac.getInstance(restoreSecretKey.getAlgorithm());
            // 初始化Mac
            mac.init(restoreSecretKey);
            // 执行摘要
            byte[] jdkResult = mac.doFinal(source.getBytes("utf-8"));
            System.out.println("JDK Hmac: " + Hex.encodeHexString(jdkResult));

            /* BC Hmac */
            HMac hmac = new HMac(new MD5Digest());
            hmac.init(new KeyParameter(keyBytes));
            hmac.update(source.getBytes("utf-8"), 0, source.getBytes("utf-8").length);
            byte[] bcResult = new byte[hmac.getMacSize()];
            hmac.doFinal(bcResult, 0);
            System.out.println("BC Hmac: " + Hex.encodeHexString(bcResult));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
